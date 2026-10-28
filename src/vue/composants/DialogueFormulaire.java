package vue.composants;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import vue.Messages;
import vue.Theme;

/**
 * Boite de dialogue modale generique contenant un Formulaire responsive.
 * La validation peut lever une exception : le message est affiche et la
 * fenetre reste ouverte pour correction.
 */
public class DialogueFormulaire extends JDialog {
    private static final long serialVersionUID = 1L;

    /** Action executee lors du clic sur "Valider". */
    public interface Validation {
        void valider() throws Exception;
    }

    private final Formulaire formulaire;
    private final BoutonModerne boutonValider;
    private Validation validation;
    private boolean valide;

    public DialogueFormulaire(Component parent, String titre, String libelleValider, int largeurMinChamp) {
        super(fenetreDe(parent), titre, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel racine = new JPanel(new BorderLayout());
        racine.setBackground(Theme.FOND);

        JLabel entete = new JLabel(titre);
        entete.setOpaque(true);
        entete.setBackground(Theme.PRIMAIRE);
        entete.setForeground(java.awt.Color.WHITE);
        entete.setFont(Theme.police(Font.BOLD, 16));
        entete.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        racine.add(entete, BorderLayout.NORTH);

        formulaire = new Formulaire(largeurMinChamp);
        PanneauDefilant conteneur = new PanneauDefilant(new BorderLayout());
        conteneur.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        conteneur.add(formulaire, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(conteneur);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.FOND);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        racine.add(sp, BorderLayout.CENTER);

        JPanel boutons = new JPanel(new WrapLayout(FlowLayout.RIGHT, 10, 6));
        boutons.setBackground(java.awt.Color.WHITE);
        boutons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDURE));
        BoutonModerne annuler = BoutonModerne.secondaire("Annuler", Theme.TEXTE_SECONDAIRE);
        boutonValider = new BoutonModerne(libelleValider, Theme.PRIMAIRE_CLAIR);
        annuler.addActionListener(e -> dispose());
        boutonValider.addActionListener(e -> valider());
        boutons.add(annuler);
        boutons.add(boutonValider);
        racine.add(boutons, BorderLayout.SOUTH);

        setContentPane(racine);
        getRootPane().setDefaultButton(boutonValider);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private static Window fenetreDe(Component c) {
        if (c instanceof Window) {
            return (Window) c;
        }
        return c == null ? null : SwingUtilities.getWindowAncestor(c);
    }

    private void valider() {
        try {
            if (validation != null) {
                validation.valider();
            }
            valide = true;
            dispose();
        } catch (Exception ex) {
            Messages.erreur(this, ex.getMessage() == null ? ex.toString() : ex.getMessage());
        }
    }

    public Formulaire getFormulaire() {
        return formulaire;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    /** Affiche la boite (bloquant) ; retourne true si validee. */
    public boolean afficher(int largeurSouhaitee) {
        pack(); // cree les ressources natives
        Dimension ecran = Toolkit.getDefaultToolkit().getScreenSize();
        int largeur = Math.min(largeurSouhaitee, (int) (ecran.width * 0.92));
        setSize(largeur, 700);
        validate();
        int hauteurContenu = getContentPane().getPreferredSize().height + getInsets().top + getInsets().bottom;
        int hauteur = Math.min(Math.max(hauteurContenu + 8, 240), (int) (ecran.height * 0.88));
        setSize(largeur, hauteur);
        setMinimumSize(new Dimension(320, 220));
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return valide;
    }
}
