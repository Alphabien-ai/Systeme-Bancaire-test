package modele;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Carte bancaire liee a un compte.
 */
public class CarteBancaire implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String EN_ATTENTE = "En attente";
    public static final String ACTIVE = "Active";
    public static final String BLOQUEE = "Bloquee";

    public static final String[] TYPES = {"Visa Classic", "Mastercard Standard", "Visa Premier", "Visa Infinite (VIP)"};

    private final String numeroCarte;
    private final Date dateExpiration;
    private final String typeCarte;
    private final CompteBancaire compte;
    private String statut = EN_ATTENTE;

    public CarteBancaire(String numeroCarte, String typeCarte, CompteBancaire compte) {
        this.numeroCarte = numeroCarte;
        this.typeCarte = typeCarte;
        this.compte = compte;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 4);
        this.dateExpiration = cal.getTime();
    }

    public void activer() {
        if (dateExpiration.before(new Date())) {
            throw new IllegalStateException("Carte expirée.");
        }
        if (!compte.isActif()) {
            throw new IllegalStateException("Le compte lié est fermé.");
        }
        statut = ACTIVE;
    }

    public void bloquer() {
        statut = BLOQUEE;
    }

    public double verifierSolde() {
        if (!ACTIVE.equals(statut)) {
            throw new IllegalStateException("La carte doit être active pour consulter le solde.");
        }
        return compte.consulterSolde();
    }

    public String getNumeroMasque() {
        if (numeroCarte.length() < 8) {
            return numeroCarte;
        }
        return numeroCarte.substring(0, 4) + " **** **** " + numeroCarte.substring(numeroCarte.length() - 4);
    }

    public String getNumeroCarte() { return numeroCarte; }
    public Date getDateExpiration() { return dateExpiration; }
    public String getTypeCarte() { return typeCarte; }
    public CompteBancaire getCompte() { return compte; }
    public String getStatut() { return statut; }
}
