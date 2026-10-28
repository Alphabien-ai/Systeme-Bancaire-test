package modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import util.Formatage;

/**
 * Classe abstraite : compte bancaire generique.
 */
public abstract class CompteBancaire implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String numeroCompte;
    private double solde;
    private final Date dateOuverture;
    private final Client titulaire;
    private Agence agence;
    private boolean actif = true;
    private final List<OperationBancaire> operations = new ArrayList<>();
    private final List<CarteBancaire> cartes = new ArrayList<>();

    protected CompteBancaire(String numeroCompte, Client titulaire) {
        this.numeroCompte = numeroCompte;
        this.titulaire = titulaire;
        this.dateOuverture = new Date();
        this.solde = 0;
    }

    // ------------------------------------------------------------ methodes UML

    public void deposer(double montant) {
        verifierActif();
        verifierMontant(montant);
        new OperationBancaire(this, OperationBancaire.DEPOT, montant, true, "Dépôt en espèces").effectuer();
    }

    /**
     * Retrait standard (respecte les regles du type de compte).
     * @return true si le retrait a ete effectue.
     */
    public boolean retirer(double montant) {
        if (!actif || montant <= 0 || !peutRetirer(montant)) {
            return false;
        }
        String type = (solde - montant < 0) ? OperationBancaire.RETRAIT_DECOUVERT : OperationBancaire.RETRAIT;
        new OperationBancaire(this, type, montant, false, "Retrait").effectuer();
        return true;
    }

    /**
     * Retrait direct au guichet de la banque reserve aux clients VIP.
     * Il est autorise MEME si le solde est deja insuffisant / negatif (compte "en faillite"),
     * dans la limite du plafond de credit VIP accorde par la banque.
     * @return true si le retrait a ete effectue.
     */
    public boolean retirerAuGuichetVIP(double montant) {
        if (!actif || montant <= 0 || !(titulaire instanceof ClientVIP)) {
            return false;
        }
        if (solde - montant < -limiteDebitVIP()) {
            return false;
        }
        new OperationBancaire(this, OperationBancaire.RETRAIT_VIP, montant, false,
                "Retrait au guichet - privilège VIP").effectuer();
        return true;
    }

    /** Limite totale de debit (en valeur positive) pour un retrait VIP au guichet. */
    public double limiteDebitVIP() {
        if (!(titulaire instanceof ClientVIP)) {
            return 0;
        }
        return getDecouvertDeBase() + ((ClientVIP) titulaire).getPlafondCreditVIP();
    }

    /**
     * Virement interne vers un autre compte (les verifications de solde
     * sont faites par l'appelant).
     */
    public void virerVers(CompteBancaire destination, double montant) {
        verifierActif();
        destination.verifierActif();
        verifierMontant(montant);
        new OperationBancaire(this, OperationBancaire.VIREMENT_EMIS, montant, false,
                "Virement vers " + destination.getNumeroCompte()).effectuer();
        new OperationBancaire(destination, OperationBancaire.VIREMENT_RECU, montant, true,
                "Virement de " + numeroCompte).effectuer();
    }

    public double consulterSolde() {
        return solde;
    }

    public String obtenirReleve() {
        StringBuilder sb = new StringBuilder();
        String ligne = "=====================================================================================\n";
        sb.append(ligne);
        sb.append("  RELEVE DE COMPTE - ").append(getTypeCompte()).append('\n');
        sb.append(ligne);
        sb.append("  Numéro      : ").append(numeroCompte).append('\n');
        sb.append("  Titulaire   : ").append(titulaire.getNomComplet())
          .append(titulaire.estVIP() ? "  (VIP)" : "").append('\n');
        sb.append("  Agence      : ").append(agence == null ? "-" : agence.getNomAgence()).append('\n');
        sb.append("  Ouverture   : ").append(Formatage.date(dateOuverture)).append('\n');
        sb.append("  Statut      : ").append(actif ? "Actif" : "Fermé").append('\n');
        sb.append(informationsSpecifiques());
        sb.append(ligne);
        sb.append("  OPERATIONS\n");
        sb.append(ligne);
        if (operations.isEmpty()) {
            sb.append("  Aucune opération.\n");
        }
        for (OperationBancaire op : operations) {
            sb.append("  ").append(op).append('\n');
        }
        sb.append(ligne);
        sb.append("  SOLDE ACTUEL : ").append(Formatage.monnaie(solde)).append('\n');
        sb.append(ligne);
        return sb.toString();
    }

    // ------------------------------------------------------------ abstraites

    /** Verifie si un retrait standard est autorise. */
    protected abstract boolean peutRetirer(double montant);

    /** Decouvert autorise par le type de compte (0 si aucun). */
    public abstract double getDecouvertDeBase();

    public abstract String getTypeCompte();

    /** Montant maximum retirable en retrait standard. */
    public abstract double getMontantDisponible();

    protected abstract String informationsSpecifiques();

    // ------------------------------------------------------------ interne

    /** Modifie le solde : utilise uniquement par OperationBancaire. */
    void appliquerMouvement(double variation) {
        solde += variation;
        solde = Math.round(solde * 100.0) / 100.0;
    }

    void enregistrerOperation(OperationBancaire op) {
        operations.add(op);
    }

    /** Debit force (frais) qui ne verifie pas le solde. */
    protected void debiterForce(String type, double montant, String libelle) {
        if (montant > 0) {
            new OperationBancaire(this, type, montant, false, libelle).effectuer();
        }
    }

    protected void crediter(String type, double montant, String libelle) {
        if (montant > 0) {
            new OperationBancaire(this, type, montant, true, libelle).effectuer();
        }
    }

    protected void verifierMontant(double montant) {
        if (montant <= 0 || Double.isNaN(montant) || Double.isInfinite(montant)) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }
    }

    protected void verifierActif() {
        if (!actif) {
            throw new IllegalStateException("Le compte " + numeroCompte + " est fermé.");
        }
    }

    void ajouterCarte(CarteBancaire carte) {
        cartes.add(carte);
    }

    // ------------------------------------------------------------ accesseurs

    public String getNumeroCompte() { return numeroCompte; }
    public Date getDateOuverture() { return dateOuverture; }
    public Client getTitulaire() { return titulaire; }
    public Agence getAgence() { return agence; }
    void setAgence(Agence agence) { this.agence = agence; }
    public boolean isActif() { return actif; }
    void setActif(boolean actif) { this.actif = actif; }
    public List<OperationBancaire> getOperations() { return Collections.unmodifiableList(operations); }
    public List<CarteBancaire> getCartes() { return Collections.unmodifiableList(cartes); }

    @Override
    public String toString() {
        return numeroCompte + " - " + getTypeCompte() + " - " + titulaire.getNomComplet()
                + " (" + Formatage.monnaie(solde) + ")";
    }
}
