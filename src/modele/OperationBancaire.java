package modele;

import java.io.Serializable;
import java.util.Date;

import util.Formatage;

/**
 * Operation bancaire (depot, retrait, virement, frais, interets...).
 * Une operation est creee puis "effectuee" sur un compte ; elle peut etre annulee.
 */
public class OperationBancaire implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEPOT = "DEPOT";
    public static final String RETRAIT = "RETRAIT";
    public static final String RETRAIT_DECOUVERT = "RETRAIT (DECOUVERT)";
    public static final String RETRAIT_VIP = "RETRAIT GUICHET VIP";
    public static final String VIREMENT_EMIS = "VIREMENT EMIS";
    public static final String VIREMENT_RECU = "VIREMENT RECU";
    public static final String FRAIS = "FRAIS DE GESTION";
    public static final String INTERETS = "INTERETS";
    public static final String ANNULATION = "ANNULATION";

    private static long compteur = 1;

    private final long idOperation;
    private final Date dateOperation;
    private final String typeOperation;
    private final double montant;
    private final double variation;      // + credit, - debit
    private final CompteBancaire compte;
    private String libelle;
    private double soldeApres;
    private boolean effectuee;
    private boolean annulee;

    public OperationBancaire(CompteBancaire compte, String typeOperation, double montant,
                             boolean credit, String libelle) {
        this.idOperation = compteur++;
        this.dateOperation = new Date();
        this.compte = compte;
        this.typeOperation = typeOperation;
        this.montant = Math.abs(montant);
        this.variation = credit ? this.montant : -this.montant;
        this.libelle = libelle == null ? typeOperation : libelle;
    }

    /** Applique l'operation sur le compte et l'ajoute a l'historique. */
    public void effectuer() {
        if (effectuee) {
            throw new IllegalStateException("Opération déjà effectuée.");
        }
        compte.appliquerMouvement(variation);
        soldeApres = compte.consulterSolde();
        effectuee = true;
        compte.enregistrerOperation(this);
    }

    /** Annule l'operation : le mouvement inverse est applique. */
    public void annuler() {
        if (!effectuee) {
            throw new IllegalStateException("Opération non effectuée.");
        }
        if (annulee) {
            throw new IllegalStateException("Opération déjà annulée.");
        }
        if (ANNULATION.equals(typeOperation)) {
            throw new IllegalStateException("Une annulation ne peut pas être annulée.");
        }
        if (VIREMENT_EMIS.equals(typeOperation) || VIREMENT_RECU.equals(typeOperation)) {
            throw new IllegalStateException("Un virement ne peut pas être annulé : effectuez un virement inverse.");
        }
        annulee = true;
        OperationBancaire inverse = new OperationBancaire(compte, ANNULATION, montant,
                variation < 0, "Annulation de l'opération #" + idOperation);
        inverse.effectuer();
    }

    public static synchronized void initialiserCompteur(long valeur) {
        if (valeur > compteur) {
            compteur = valeur;
        }
    }

    public long getIdOperation() { return idOperation; }
    public Date getDateOperation() { return dateOperation; }
    public String getTypeOperation() { return typeOperation; }
    public double getMontant() { return montant; }
    public double getVariation() { return variation; }
    public CompteBancaire getCompte() { return compte; }
    public String getLibelle() { return libelle; }
    public double getSoldeApres() { return soldeApres; }
    public boolean isEffectuee() { return effectuee; }
    public boolean isAnnulee() { return annulee; }
    public boolean estCredit() { return variation > 0; }

    @Override
    public String toString() {
        return String.format("#%d  %s  %-22s %s%s  -> solde %s%s",
                idOperation, Formatage.dateHeure(dateOperation), typeOperation,
                estCredit() ? "+" : "-", Formatage.monnaie(montant),
                Formatage.monnaie(soldeApres), annulee ? "  [ANNULEE]" : "");
    }
}
