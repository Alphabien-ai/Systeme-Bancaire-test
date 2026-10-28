package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilitaires de formatage (monnaie en Ariary, dates).
 */
public final class Formatage {

    private static final DecimalFormat FORMAT_MONNAIE;
    private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat FORMAT_DATE_HEURE = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final String DEVISE = "Ar";

    static {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        symboles.setDecimalSeparator(',');
        FORMAT_MONNAIE = new DecimalFormat("#,##0.00", symboles);
    }

    private Formatage() {
    }

    public static String monnaie(double montant) {
        return FORMAT_MONNAIE.format(montant) + " " + DEVISE;
    }

    public static String date(Date d) {
        return d == null ? "-" : FORMAT_DATE.format(d);
    }

    public static String dateHeure(Date d) {
        return d == null ? "-" : FORMAT_DATE_HEURE.format(d);
    }

    /**
     * Convertit une saisie utilisateur ("1 500,50" ou "1500.50") en double.
     */
    public static double lireMontant(String texte) throws NumberFormatException {
        if (texte == null) {
            throw new NumberFormatException("Montant vide");
        }
        String nettoye = texte.trim().replace(" ", "").replace("\u00A0", "")
                .replace(DEVISE, "").replace(',', '.');
        if (nettoye.isEmpty()) {
            throw new NumberFormatException("Montant vide");
        }
        return Double.parseDouble(nettoye);
    }

    public static Date lireDate(String texte) throws ParseException {
        return FORMAT_DATE.parse(texte.trim());
    }
}
