package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exception.BanqueException;
import exception.SoldeInsuffisantException;
import modele.Agence;
import modele.Banque;
import modele.CarteBancaire;
import modele.Client;
import modele.ClientVIP;
import modele.CompteBancaire;
import modele.CompteCourant;
import modele.CompteEpargne;
import modele.OperationBancaire;
import modele.ServiceBancaire;
import util.Formatage;

/**
 * Couche service : regroupe toutes les regles metier appelees par l'interface
 * graphique, valide les saisies et sauvegarde automatiquement les donnees.
 */
public class BanqueService {

    public static final String FICHIER_DONNEES = "banque_donnees.dat";

    /** Ecouteur notifie apres chaque modification des donnees. */
    public interface EcouteurDonnees {
        void donneesModifiees();
    }

    private final Persistance persistance = new Persistance(FICHIER_DONNEES);
    private final List<EcouteurDonnees> ecouteurs = new ArrayList<>();
    private Banque banque;
    private String dernierAvertissement;

    public BanqueService() {
        chargerOuInitialiser();
    }

    private void chargerOuInitialiser() {
        if (persistance.existe()) {
            try {
                banque = persistance.charger();
                return;
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                dernierAvertissement = "Fichier de données illisible, données de démonstration rechargées.\n" + e.getMessage();
            }
        }
        banque = DonneesDemo.creer();
        sauvegarderSilencieux();
    }

    public Banque getBanque() {
        return banque;
    }

    public String consommerAvertissement() {
        String a = dernierAvertissement;
        dernierAvertissement = null;
        return a;
    }

    public void ajouterEcouteur(EcouteurDonnees e) {
        ecouteurs.add(e);
    }

    private void modifie() {
        sauvegarderSilencieux();
        for (EcouteurDonnees e : new ArrayList<>(ecouteurs)) {
            e.donneesModifiees();
        }
    }

    public void sauvegarder() throws IOException {
        persistance.sauvegarder(banque);
    }

    private void sauvegarderSilencieux() {
        try {
            persistance.sauvegarder(banque);
        } catch (IOException e) {
            dernierAvertissement = "Sauvegarde impossible : " + e.getMessage();
        }
    }

    public void reinitialiserDemo() {
        banque = DonneesDemo.creer();
        modifie();
    }

    public String getCheminDonnees() {
        return persistance.getChemin();
    }

    // ================================================================ CLIENTS

    public Client creerClient(String nom, String prenom, String adresse, String telephone, Agence agence,
                              boolean vip, double plafondVIP, String conseiller) throws BanqueException {
        validerTexte(nom, "Le nom");
        validerTexte(prenom, "Le prénom");
        validerTelephone(telephone);
        if (agence == null) {
            throw new BanqueException("Veuillez choisir une agence.");
        }
        Client c;
        if (vip) {
            if (plafondVIP <= 0) {
                throw new BanqueException("Le plafond de crédit VIP doit être positif.");
            }
            c = new ClientVIP(banque.genererIdClient(), nom.trim(), prenom.trim(), nettoyer(adresse),
                    telephone.trim(), plafondVIP, nettoyer(conseiller));
        } else {
            c = new Client(banque.genererIdClient(), nom.trim(), prenom.trim(), nettoyer(adresse), telephone.trim());
        }
        agence.gererClient(c);
        modifie();
        return c;
    }

    public void modifierClient(Client c, String nom, String prenom, String adresse, String telephone,
                               Agence agence, double plafondVIP, String conseiller) throws BanqueException {
        validerTexte(nom, "Le nom");
        validerTexte(prenom, "Le prénom");
        validerTelephone(telephone);
        c.setNom(nom.trim());
        c.setPrenom(prenom.trim());
        c.setAdresse(nettoyer(adresse));
        c.setTelephone(telephone.trim());
        if (agence != null && agence != c.getAgence()) {
            agence.gererClient(c);
        }
        if (c instanceof ClientVIP) {
            if (plafondVIP <= 0) {
                throw new BanqueException("Le plafond de crédit VIP doit être positif.");
            }
            ((ClientVIP) c).setPlafondCreditVIP(plafondVIP);
            ((ClientVIP) c).setConseillerDedie(nettoyer(conseiller));
        }
        modifie();
    }

    /**
     * Supprime un client : tous ses comptes doivent etre fermes.
     * Le client reste dans l'historique des comptes fermes, mais n'est plus rattache a une agence.
     */
    public void supprimerClient(Client c) throws BanqueException {
        for (CompteBancaire compte : c.getComptes()) {
            if (compte.isActif()) {
                throw new BanqueException("Le client possède encore le compte actif " + compte.getNumeroCompte()
                        + ".\nFermez tous ses comptes avant de le supprimer.");
            }
        }
        for (ServiceBancaire s : new ArrayList<>(c.getServices())) {
            s.resilier(c);
        }
        Agence a = c.getAgence();
        if (a != null) {
            a.supprimerClient(c);
        }
        modifie();
    }

    // ================================================================ COMPTES

    public CompteBancaire ouvrirCompteCourant(Client c, double decouvert, double frais, double depotInitial)
            throws BanqueException {
        verifierClient(c);
        if (decouvert < 0 || frais < 0 || depotInitial < 0) {
            throw new BanqueException("Les montants ne peuvent pas être négatifs.");
        }
        if (decouvert > 0 && !c.estVIP() && decouvert > 5_000_000) {
            throw new BanqueException("Le découvert d'un client standard est limité à " + Formatage.monnaie(5_000_000) + ".");
        }
        CompteCourant cc = new CompteCourant(banque.genererNumeroCompte(true), c, decouvert, frais);
        c.getAgence().ouvrirCompte(cc);
        if (depotInitial > 0) {
            cc.deposer(depotInitial);
        }
        modifie();
        return cc;
    }

    public CompteBancaire ouvrirCompteEpargne(Client c, double taux, double minimum, double depotInitial)
            throws BanqueException {
        verifierClient(c);
        if (taux < 0 || taux > 25) {
            throw new BanqueException("Le taux d'intérêt doit être compris entre 0 et 25 %.");
        }
        if (minimum < 0 || depotInitial < 0) {
            throw new BanqueException("Les montants ne peuvent pas être négatifs.");
        }
        if (depotInitial < minimum) {
            throw new BanqueException("Le dépôt initial doit être au moins égal au solde minimum ("
                    + Formatage.monnaie(minimum) + ").");
        }
        CompteEpargne ce = new CompteEpargne(banque.genererNumeroCompte(false), c, taux, minimum);
        c.getAgence().ouvrirCompte(ce);
        if (depotInitial > 0) {
            ce.deposer(depotInitial);
        }
        modifie();
        return ce;
    }

    public void fermerCompte(CompteBancaire compte) throws BanqueException {
        if (!compte.isActif()) {
            throw new BanqueException("Ce compte est déjà fermé.");
        }
        try {
            compte.getAgence().fermerCompte(compte);
        } catch (IllegalStateException e) {
            throw new BanqueException(e.getMessage());
        }
        modifie();
    }

    // ================================================================ OPERATIONS

    public void deposer(CompteBancaire compte, double montant) throws BanqueException {
        verifierCompteActif(compte);
        verifierMontant(montant);
        compte.deposer(montant);
        modifie();
    }

    /**
     * Retrait standard. Si le solde est insuffisant, une SoldeInsuffisantException
     * est levee en indiquant si le client est VIP (retrait guichet possible).
     */
    public void retirer(CompteBancaire compte, double montant) throws BanqueException {
        verifierCompteActif(compte);
        verifierMontant(montant);
        if (!compte.retirer(montant)) {
            boolean vip = compte.getTitulaire().estVIP();
            String msg = "Solde insuffisant.\nMontant disponible : " + Formatage.monnaie(compte.getMontantDisponible())
                    + "\nMontant demande : " + Formatage.monnaie(montant);
            if (vip) {
                msg += "\n\nCe client est VIP : il peut effectuer un retrait direct au guichet "
                        + "(limite de débit : -" + Formatage.monnaie(compte.limiteDebitVIP()) + ").";
            }
            throw new SoldeInsuffisantException(msg, vip);
        }
        modifie();
    }

    /** Retrait direct au guichet reserve aux clients VIP (solde insuffisant accepte). */
    public void retirerAuGuichetVIP(CompteBancaire compte, double montant) throws BanqueException {
        verifierCompteActif(compte);
        verifierMontant(montant);
        Client t = compte.getTitulaire();
        if (!(t instanceof ClientVIP)) {
            throw new BanqueException("Le retrait direct au guichet avec solde insuffisant est réservé aux clients VIP.");
        }
        if (!((ClientVIP) t).retirerAuGuichet(compte, montant)) {
            double possible = compte.consulterSolde() + compte.limiteDebitVIP();
            throw new BanqueException("Plafond de crédit VIP atteint.\nLimite de débit : -"
                    + Formatage.monnaie(compte.limiteDebitVIP())
                    + "\nMontant maximum retirable au guichet : " + Formatage.monnaie(Math.max(0, possible)));
        }
        modifie();
    }

    public void virer(CompteBancaire source, CompteBancaire destination, double montant) throws BanqueException {
        verifierCompteActif(source);
        verifierCompteActif(destination);
        verifierMontant(montant);
        if (source == destination) {
            throw new BanqueException("Les comptes source et destination doivent être différents.");
        }
        if (montant > source.getMontantDisponible()) {
            throw new SoldeInsuffisantException("Solde insuffisant pour le virement.\nDisponible : "
                    + Formatage.monnaie(source.getMontantDisponible()), false);
        }
        source.virerVers(destination, montant);
        modifie();
    }

    public void annulerOperation(OperationBancaire op) throws BanqueException {
        try {
            op.annuler();
        } catch (IllegalStateException e) {
            throw new BanqueException(e.getMessage());
        }
        modifie();
    }

    public String traitementMensuel() {
        String r = banque.gererCompte();
        modifie();
        return r;
    }

    // ================================================================ CARTES

    public CarteBancaire demanderCarte(CompteBancaire compte, String type) throws BanqueException {
        verifierCompteActif(compte);
        if (type != null && type.contains("VIP") && !compte.getTitulaire().estVIP()) {
            throw new BanqueException("La carte " + type + " est réservée aux clients VIP.");
        }
        CarteBancaire carte = compte.getTitulaire().demanderCarte(banque.genererNumeroCarte(), compte, type);
        modifie();
        return carte;
    }

    public void activerCarte(CarteBancaire carte) throws BanqueException {
        try {
            carte.activer();
        } catch (IllegalStateException e) {
            throw new BanqueException(e.getMessage());
        }
        modifie();
    }

    public void bloquerCarte(CarteBancaire carte) {
        carte.bloquer();
        modifie();
    }

    // ================================================================ AGENCES / SERVICES

    public Agence creerAgence(String nom, String adresse, String telephone) throws BanqueException {
        validerTexte(nom, "Le nom de l'agence");
        Agence a = new Agence(banque.genererCodeAgence(), nom.trim(), nettoyer(adresse), nettoyer(telephone));
        banque.ajouterAgence(a);
        modifie();
        return a;
    }

    public void modifierAgence(Agence a, String nom, String adresse, String telephone) throws BanqueException {
        validerTexte(nom, "Le nom de l'agence");
        a.setNomAgence(nom.trim());
        a.setAdresse(nettoyer(adresse));
        a.setTelephone(nettoyer(telephone));
        modifie();
    }

    public ServiceBancaire creerService(String nom, String description, double tarif) throws BanqueException {
        validerTexte(nom, "Le nom du service");
        if (tarif < 0) {
            throw new BanqueException("Le tarif ne peut pas être négatif.");
        }
        ServiceBancaire s = new ServiceBancaire(banque.genererCodeService(), nom.trim(), nettoyer(description), tarif);
        banque.ajouterService(s);
        modifie();
        return s;
    }

    public void souscrire(ServiceBancaire s, Client c) throws BanqueException {
        try {
            s.souscrire(c);
        } catch (IllegalStateException e) {
            throw new BanqueException(e.getMessage());
        }
        modifie();
    }

    public void resilier(ServiceBancaire s, Client c) throws BanqueException {
        try {
            s.resilier(c);
        } catch (IllegalStateException e) {
            throw new BanqueException(e.getMessage());
        }
        modifie();
    }

    public void modifierBanque(String nom, String adresse, double capital) throws BanqueException {
        validerTexte(nom, "Le nom de la banque");
        if (capital < 0) {
            throw new BanqueException("Le capital ne peut pas être négatif.");
        }
        banque.setNom(nom.trim());
        banque.setAdresse(nettoyer(adresse));
        banque.setCapital(capital);
        modifie();
    }

    // ================================================================ VALIDATIONS

    private void verifierClient(Client c) throws BanqueException {
        if (c == null) {
            throw new BanqueException("Veuillez sélectionner un client.");
        }
        if (c.getAgence() == null) {
            throw new BanqueException("Le client n'est rattaché à aucune agence.");
        }
    }

    private void verifierCompteActif(CompteBancaire c) throws BanqueException {
        if (c == null) {
            throw new BanqueException("Veuillez sélectionner un compte.");
        }
        if (!c.isActif()) {
            throw new BanqueException("Le compte " + c.getNumeroCompte() + " est fermé.");
        }
    }

    private void verifierMontant(double m) throws BanqueException {
        if (Double.isNaN(m) || Double.isInfinite(m) || m <= 0) {
            throw new BanqueException("Le montant doit être strictement positif.");
        }
        if (m > 1_000_000_000_000.0) {
            throw new BanqueException("Montant trop élevé.");
        }
    }

    private void validerTexte(String t, String champ) throws BanqueException {
        if (t == null || t.trim().isEmpty()) {
            throw new BanqueException(champ + " est obligatoire.");
        }
        if (t.trim().length() > 60) {
            throw new BanqueException(champ + " est trop long (60 caractères max).");
        }
    }

    private void validerTelephone(String t) throws BanqueException {
        if (t == null || t.trim().isEmpty()) {
            throw new BanqueException("Le téléphone est obligatoire.");
        }
        if (!t.trim().matches("[+0-9 ().-]{6,20}")) {
            throw new BanqueException("Numéro de téléphone invalide.");
        }
    }

    private static String nettoyer(String s) {
        return s == null ? "" : s.trim();
    }
}
