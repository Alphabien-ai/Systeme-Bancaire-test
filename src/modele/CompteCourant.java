package modele;

import util.Formatage;

/**
 * Compte courant : autorise un decouvert et preleve des frais de gestion.
 */
public class CompteCourant extends CompteBancaire {
    private static final long serialVersionUID = 1L;
    /** Taux des agios appliques au solde negatif (par mois). */
    public static final double TAUX_AGIOS = 0.015;

    private double decouvertAutorise;
    private double fraisGestion;

    public CompteCourant(String numeroCompte, Client titulaire, double decouvertAutorise, double fraisGestion) {
        super(numeroCompte, titulaire);
        this.decouvertAutorise = Math.max(0, decouvertAutorise);
        this.fraisGestion = Math.max(0, fraisGestion);
    }

    @Override
    protected boolean peutRetirer(double montant) {
        return consulterSolde() - montant >= -decouvertAutorise;
    }

    /**
     * Effectue un retrait en utilisant le decouvert autorise.
     * @return true si le retrait a ete effectue.
     */
    public boolean effectuerDecouvert(double montant) {
        return retirer(montant);
    }

    /** Frais mensuels = frais de gestion + agios sur solde negatif. */
    public double calculerFrais() {
        double agios = consulterSolde() < 0 ? Math.abs(consulterSolde()) * TAUX_AGIOS : 0;
        return Math.round((fraisGestion + agios) * 100.0) / 100.0;
    }

    public double appliquerFrais() {
        double frais = calculerFrais();
        debiterForce(OperationBancaire.FRAIS, frais, "Frais de gestion mensuels et agios");
        return frais;
    }

    @Override
    public double getDecouvertDeBase() {
        return decouvertAutorise;
    }

    @Override
    public double getMontantDisponible() {
        return Math.max(0, consulterSolde() + decouvertAutorise);
    }

    @Override
    public String getTypeCompte() {
        return "Compte Courant";
    }

    @Override
    protected String informationsSpecifiques() {
        return "  Découvert   : " + Formatage.monnaie(decouvertAutorise) + '\n'
             + "  Frais/mois  : " + Formatage.monnaie(fraisGestion) + '\n';
    }

    public double getDecouvertAutorise() { return decouvertAutorise; }
    public void setDecouvertAutorise(double d) { this.decouvertAutorise = Math.max(0, d); }
    public double getFraisGestion() { return fraisGestion; }
    public void setFraisGestion(double f) { this.fraisGestion = Math.max(0, f); }
}
