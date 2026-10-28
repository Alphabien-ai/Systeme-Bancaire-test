package modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import util.Formatage;

/**
 * Banque : racine du modele, possede les agences et les services.
 */
public class Banque implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String idBanque;
    private String nom;
    private String adresse;
    private double capital;
    private final List<Agence> agences = new ArrayList<>();
    private final List<ServiceBancaire> services = new ArrayList<>();

    private int compteurClient = 1;
    private int compteurCompte = 1;
    private int compteurAgence = 1;
    private int compteurService = 1;
    private long dernierIdOperation = 1;

    public Banque(String idBanque, String nom, String adresse, double capital) {
        this.idBanque = idBanque;
        this.nom = nom;
        this.adresse = adresse;
        this.capital = capital;
    }

    public void ajouterAgence(Agence agence) {
        if (!agences.contains(agence)) {
            agences.add(agence);
            agence.setBanque(this);
        }
    }

    public void ajouterService(ServiceBancaire service) {
        if (!services.contains(service)) {
            services.add(service);
        }
    }

    /**
     * Gestion periodique des comptes : applique les frais des comptes courants
     * et les interets des comptes epargne. Retourne un resume.
     */
    public String gererCompte() {
        double totalFrais = 0;
        double totalInterets = 0;
        int nb = 0;
        for (CompteBancaire c : getTousLesComptes()) {
            if (!c.isActif()) {
                continue;
            }
            if (c instanceof CompteCourant) {
                totalFrais += ((CompteCourant) c).appliquerFrais();
            } else if (c instanceof CompteEpargne) {
                totalInterets += ((CompteEpargne) c).appliquerInterets();
            }
            nb++;
        }
        return nb + " compte(s) traité(s).\nFrais prélevés : " + Formatage.monnaie(totalFrais)
                + "\nIntérêts versés : " + Formatage.monnaie(totalInterets);
    }

    public String consulterRapport() {
        StringBuilder sb = new StringBuilder();
        String l = "==============================================================\n";
        sb.append(l).append("  RAPPORT GENERAL - ").append(nom.toUpperCase()).append('\n');
        sb.append("  Généré le ").append(Formatage.dateHeure(new Date())).append('\n').append(l);
        sb.append("  Identifiant     : ").append(idBanque).append('\n');
        sb.append("  Siège           : ").append(adresse).append('\n');
        sb.append("  Capital         : ").append(Formatage.monnaie(capital)).append('\n');
        sb.append("  Agences         : ").append(agences.size()).append('\n');
        sb.append("  Clients         : ").append(getTousLesClients().size())
          .append(" (dont ").append(getNombreVIP()).append(" VIP)\n");
        sb.append("  Comptes actifs  : ").append(getNombreComptesActifs()).append('\n');
        sb.append("  Total depots    : ").append(Formatage.monnaie(getTotalDepots())).append('\n');
        sb.append("  Encours négatif : ").append(Formatage.monnaie(getEncoursNegatif())).append('\n');
        sb.append(l).append("  DETAIL PAR AGENCE\n").append(l);
        for (Agence a : agences) {
            sb.append(String.format("  %-6s %-28s clients: %3d  comptes: %3d  depots: %s%n",
                    a.getCodeAgence(), a.getNomAgence(), a.getClients().size(),
                    a.getComptes().size(), Formatage.monnaie(a.getTotalDepots())));
        }
        sb.append(l).append("  COMPTES A DECOUVERT / VIP EN DEBIT\n").append(l);
        boolean aucun = true;
        for (CompteBancaire c : getTousLesComptes()) {
            if (c.isActif() && c.consulterSolde() < 0) {
                aucun = false;
                sb.append(String.format("  %-12s %-28s %s%s%n", c.getNumeroCompte(),
                        c.getTitulaire().getNomComplet(), Formatage.monnaie(c.consulterSolde()),
                        c.getTitulaire().estVIP() ? "  (VIP)" : ""));
            }
        }
        if (aucun) {
            sb.append("  Aucun compte débiteur.\n");
        }
        sb.append(l).append("  SERVICES\n").append(l);
        for (ServiceBancaire s : services) {
            sb.append(String.format("  %-6s %-30s %3d souscripteur(s)%n", s.getCodeService(),
                    s.getNomService(), s.getSouscripteurs().size()));
        }
        sb.append(l);
        return sb.toString();
    }

    // ------------------------------------------------------------ requetes

    public List<Client> getTousLesClients() {
        List<Client> liste = new ArrayList<>();
        for (Agence a : agences) {
            liste.addAll(a.getClients());
        }
        return liste;
    }

    public List<CompteBancaire> getTousLesComptes() {
        List<CompteBancaire> liste = new ArrayList<>();
        for (Agence a : agences) {
            liste.addAll(a.getComptes());
        }
        return liste;
    }

    public List<CompteBancaire> getComptesActifs() {
        List<CompteBancaire> liste = new ArrayList<>();
        for (CompteBancaire c : getTousLesComptes()) {
            if (c.isActif()) {
                liste.add(c);
            }
        }
        return liste;
    }

    public List<CarteBancaire> getToutesLesCartes() {
        List<CarteBancaire> liste = new ArrayList<>();
        for (Client c : getTousLesClients()) {
            liste.addAll(c.getCartes());
        }
        return liste;
    }

    public List<OperationBancaire> getToutesLesOperations() {
        List<OperationBancaire> liste = new ArrayList<>();
        for (CompteBancaire c : getTousLesComptes()) {
            liste.addAll(c.getOperations());
        }
        liste.sort((a, b) -> Long.compare(b.getIdOperation(), a.getIdOperation()));
        return liste;
    }

    public CompteBancaire trouverCompte(String numero) {
        for (CompteBancaire c : getTousLesComptes()) {
            if (c.getNumeroCompte().equalsIgnoreCase(numero.trim())) {
                return c;
            }
        }
        return null;
    }

    public int getNombreVIP() {
        int n = 0;
        for (Client c : getTousLesClients()) {
            if (c.estVIP()) {
                n++;
            }
        }
        return n;
    }

    public int getNombreComptesActifs() {
        return getComptesActifs().size();
    }

    public double getTotalDepots() {
        double t = 0;
        for (CompteBancaire c : getComptesActifs()) {
            t += c.consulterSolde();
        }
        return t;
    }

    public double getEncoursNegatif() {
        double t = 0;
        for (CompteBancaire c : getComptesActifs()) {
            if (c.consulterSolde() < 0) {
                t += c.consulterSolde();
            }
        }
        return t;
    }

    // ------------------------------------------------------------ generateurs

    public String genererIdClient() {
        return String.format("CL%04d", compteurClient++);
    }

    public String genererNumeroCompte(boolean courant) {
        return String.format("%s-%06d", courant ? "CC" : "CE", compteurCompte++);
    }

    public String genererCodeAgence() {
        return String.format("AG%02d", compteurAgence++);
    }

    public String genererCodeService() {
        return String.format("SV%02d", compteurService++);
    }

    public String genererNumeroCarte() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder("4970");
        for (int i = 0; i < 12; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    /** A appeler avant la sauvegarde pour memoriser le compteur d'operations. */
    public void memoriserCompteurs() {
        for (OperationBancaire op : getToutesLesOperations()) {
            if (op.getIdOperation() >= dernierIdOperation) {
                dernierIdOperation = op.getIdOperation() + 1;
            }
        }
    }

    /** A appeler apres le chargement. */
    public void restaurerCompteurs() {
        memoriserCompteurs();
        OperationBancaire.initialiserCompteur(dernierIdOperation);
    }

    public String getIdBanque() { return idBanque; }
    public String getNom() { return nom; }
    public void setNom(String n) { this.nom = n; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String a) { this.adresse = a; }
    public double getCapital() { return capital; }
    public void setCapital(double c) { this.capital = c; }
    public List<Agence> getAgences() { return Collections.unmodifiableList(agences); }
    public List<ServiceBancaire> getServices() { return Collections.unmodifiableList(services); }
}
