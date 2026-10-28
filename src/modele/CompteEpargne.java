package modele;

import util.Formatage;

/**
 * Compte epargne : remunere par un taux d'interet, impose un solde minimum.
 */
public class CompteEpargne extends CompteBancaire {
    private static final long serialVersionUID = 1L;

    private double tauxInteret;     // en % annuel
    private double montantMinimum;

    public CompteEpargne(String numeroCompte, Client titulaire, double tauxInteret, double montantMinimum) {
        super(numeroCompte, titulaire);
        this.tauxInteret = Math.max(0, tauxInteret);
        this.montantMinimum = Math.max(0, montantMinimum);
    }

    /** Interets mensuels = solde x taux annuel / 12. */
    public double calculerInteret() {
        if (consulterSolde() <= 0) {
            return 0;
        }
        return Math.round(consulterSolde() * tauxInteret / 100.0 / 12.0 * 100.0) / 100.0;
    }

    public double appliquerInterets() {
        double interets = calculerInteret();
        crediter(OperationBancaire.INTERETS, interets, "Intérêts mensuels (" + tauxInteret + " %/an)");
        return interets;
    }

    public void effectuerVersement(double montant) {
        deposer(montant);
    }

    public boolean effectuerRetrait(double montant) {
        return retirer(montant);
    }

    @Override
    protected boolean peutRetirer(double montant) {
        return consulterSolde() - montant >= montantMinimum;
    }

    @Override
    public double getDecouvertDeBase() {
        return 0;
    }

    @Override
    public double getMontantDisponible() {
        return Math.max(0, consulterSolde() - montantMinimum);
    }

    @Override
    public String getTypeCompte() {
        return "Compte Epargne";
    }

    @Override
    protected String informationsSpecifiques() {
        return "  Taux        : " + tauxInteret + " % / an\n"
             + "  Minimum     : " + Formatage.monnaie(montantMinimum) + '\n';
    }

    public double getTauxInteret() { return tauxInteret; }
    public void setTauxInteret(double t) { this.tauxInteret = Math.max(0, t); }
    public double getMontantMinimum() { return montantMinimum; }
    public void setMontantMinimum(double m) { this.montantMinimum = Math.max(0, m); }
}
