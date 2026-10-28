package vue;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Boites de dialogue standardisees.
 */
public final class Messages {

    private Messages() {
    }

    public static void erreur(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void succes(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Opération réussie", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void info(Component parent, String titre, String message) {
        JOptionPane.showMessageDialog(parent, message, titre, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmer(Component parent, String titre, String message) {
        return JOptionPane.showConfirmDialog(parent, message, titre, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static boolean confirmerAttention(Component parent, String titre, String message) {
        return JOptionPane.showConfirmDialog(parent, message, titre, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Affiche un long texte (releve, details) dans une zone defilante. */
    public static void texteLong(Component parent, String titre, String texte) {
        JTextArea zone = new JTextArea(texte);
        zone.setEditable(false);
        zone.setFont(Theme.policeMono(12.5f));
        zone.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(zone);
        sp.setPreferredSize(new java.awt.Dimension(
                Math.min(860, (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.8)),
                Math.min(520, (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.6))));
        JOptionPane.showMessageDialog(parent, sp, titre, JOptionPane.PLAIN_MESSAGE);
    }

    static Font gras() {
        return Theme.police(Font.BOLD, 13);
    }
}
