package vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/**
 * Charte graphique centralisee (couleurs, polices, Look & Feel).
 */
public final class Theme {

    public static final Color PRIMAIRE = new Color(18, 52, 102);
    public static final Color PRIMAIRE_CLAIR = new Color(39, 89, 160);
    public static final Color BARRE_LATERALE = new Color(14, 32, 64);
    public static final Color BARRE_SURVOL = new Color(28, 52, 96);
    public static final Color OR = new Color(212, 168, 44);
    public static final Color OR_FONCE = new Color(160, 116, 10);
    public static final Color OR_CLAIR = new Color(253, 245, 220);
    public static final Color SUCCES = new Color(25, 135, 84);
    public static final Color DANGER = new Color(200, 45, 60);
    public static final Color AVERTISSEMENT = new Color(214, 120, 0);
    public static final Color INFO = new Color(13, 130, 170);
    public static final Color VIOLET = new Color(111, 66, 193);
    public static final Color FOND = new Color(242, 245, 250);
    public static final Color CARTE = Color.WHITE;
    public static final Color BORDURE = new Color(222, 227, 235);
    public static final Color TEXTE = new Color(30, 37, 48);
    public static final Color TEXTE_SECONDAIRE = new Color(108, 117, 134);
    public static final Color LIGNE_ALTERNEE = new Color(247, 249, 252);
    public static final Color SELECTION = new Color(214, 228, 250);
    public static final Color ENTETE_TABLE = new Color(235, 240, 248);
    public static final Color DESACTIVE = new Color(190, 196, 206);

    private static final String FAMILLE = choisirFamille(Arrays.asList(
            "Segoe UI", "Inter", "Roboto", "Open Sans", "Ubuntu", "Helvetica Neue", "DejaVu Sans"), Font.SANS_SERIF);
    private static final String FAMILLE_MONO = choisirFamille(Arrays.asList(
            "Consolas", "JetBrains Mono", "Fira Code", "DejaVu Sans Mono", "Menlo"), Font.MONOSPACED);

    private Theme() {
    }

    private static String choisirFamille(List<String> preferees, String defaut) {
        try {
            List<String> dispo = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());
            for (String f : preferees) {
                if (dispo.contains(f)) {
                    return f;
                }
            }
        } catch (Exception ignore) {
            // environnement sans polices : on garde la police par defaut
        }
        return defaut;
    }

    public static Font police(int style, float taille) {
        return new Font(FAMILLE, style, Math.round(taille));
    }

    public static Font policeMono(float taille) {
        return new Font(FAMILLE_MONO, Font.PLAIN, Math.round(taille));
    }

    /** Applique le Look & Feel Nimbus personnalise (repli sur le L&F systeme). */
    public static void appliquerLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.put("nimbusBase", new ColorUIResource(PRIMAIRE));
                    UIManager.put("nimbusBlueGrey", new ColorUIResource(new Color(200, 207, 220)));
                    UIManager.put("control", new ColorUIResource(FOND));
                    UIManager.put("nimbusSelectionBackground", new ColorUIResource(PRIMAIRE_CLAIR));
                    UIManager.put("nimbusFocus", new ColorUIResource(PRIMAIRE_CLAIR));
                    UIManager.put("text", new ColorUIResource(TEXTE));
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignore) {
                // L&F par defaut
            }
        }
        FontUIResource base = new FontUIResource(police(Font.PLAIN, 13));
        String[] cles = {"Label.font", "Button.font", "TextField.font", "PasswordField.font", "ComboBox.font",
                "Table.font", "TableHeader.font", "TextArea.font", "CheckBox.font", "RadioButton.font",
                "OptionPane.messageFont", "OptionPane.buttonFont", "List.font", "Menu.font", "MenuItem.font",
                "PopupMenu.font", "ToolTip.font", "Spinner.font", "FormattedTextField.font"};
        for (String c : cles) {
            UIManager.put(c, base);
        }
        UIManager.put("Table.alternateRowColor", new ColorUIResource(LIGNE_ALTERNEE));
    }

    public static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
