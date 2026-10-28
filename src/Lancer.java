import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import service.BanqueService;
import vue.FenetreConnexion;
import vue.FenetrePrincipale;
import vue.Messages;
import vue.Theme;

/**
 * ============================================================
 *  POINT D'ENTREE DE L'APPLICATION - SYSTEME BANCAIRE ASJA
 * ============================================================
 *  Compilation : javac -encoding UTF-8 -d bin -sourcepath src src/Lancer.java
 *  Execution   : java -cp bin Lancer
 *  (ou utiliser lancer.bat sous Windows / lancer.sh sous Linux-macOS)
 *
 *  Identifiants : admin / admin
 *  Option       : java -cp bin Lancer --sans-connexion
 */
public class Lancer {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        final boolean sansConnexion = args.length > 0 && "--sans-connexion".equals(args[0]);

        SwingUtilities.invokeLater(() -> {
            Theme.appliquerLookAndFeel();

            BanqueService service;
            try {
                service = new BanqueService();
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "Impossible de démarrer : " + e.getMessage(),
                        "Erreur fatale", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            if (!sansConnexion) {
                FenetreConnexion connexion = new FenetreConnexion(null);
                if (!connexion.authentifier()) {
                    System.exit(0);
                    return;
                }
            }

            FenetrePrincipale fenetre = new FenetrePrincipale(service);
            fenetre.setVisible(true);

            String avertissement = service.consommerAvertissement();
            if (avertissement != null) {
                Messages.info(fenetre, "Avertissement", avertissement);
            }
        });
    }
}
