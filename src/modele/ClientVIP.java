package modele;

/**
 * Client VIP : beneficie d'un plafond de credit special lui permettant
 * de retirer directement au guichet de la banque meme lorsque son solde
 * est insuffisant ou deja negatif (compte "en faillite").
 */
public class ClientVIP extends Client {
    private static final long serialVersionUID = 1L;

    private double plafondCreditVIP;
    private String conseillerDedie;

    public ClientVIP(String idClient, String nom, String prenom, String adresse, String telephone,
                     double plafondCreditVIP, String conseillerDedie) {
        super(idClient, nom, prenom, adresse, telephone);
        this.plafondCreditVIP = Math.max(0, plafondCreditVIP);
        this.conseillerDedie = conseillerDedie;
    }

    /**
     * Retrait direct au guichet : autorise meme si le solde est insuffisant.
     */
    public boolean retirerAuGuichet(CompteBancaire compte, double montant) {
        if (!getComptes().contains(compte)) {
            return false;
        }
        return compte.retirerAuGuichetVIP(montant);
    }

    @Override
    public boolean estVIP() {
        return true;
    }

    @Override
    public String getCategorie() {
        return "VIP";
    }

    public double getPlafondCreditVIP() { return plafondCreditVIP; }
    public void setPlafondCreditVIP(double p) { this.plafondCreditVIP = Math.max(0, p); }
    public String getConseillerDedie() { return conseillerDedie; }
    public void setConseillerDedie(String c) { this.conseillerDedie = c; }
}
