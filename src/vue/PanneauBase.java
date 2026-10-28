package vue;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import modele.Banque;
import service.BanqueService;
import vue.composants.WrapLayout;

/**
 * Base commune des ecrans : titre, sous-titre et barre d'actions responsive.
 */
public abstract class PanneauBase extends JPanel implements Rafraichissable {
    private static final long serialVersionUID = 1L;

    protected final BanqueService service;
    protected final FenetrePrincipale fenetre;
    protected final JPanel barreActions;
    private final JLabel sousTitre;

    protected PanneauBase(BanqueService service, FenetrePrincipale fenetre, String titre, String sousTitre) {
        super(new BorderLayout(0, 12));
        this.service = service;
        this.fenetre = fenetre;
        setBackground(Theme.FOND);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel entete = new JPanel(new BorderLayout(0, 8));
        entete.setOpaque(false);
        JPanel textes = new JPanel(new BorderLayout(0, 2));
        textes.setOpaque(false);
        JLabel t = new JLabel(titre);
        t.setFont(Theme.police(Font.BOLD, 22));
        t.setForeground(Theme.TEXTE);
        this.sousTitre = new JLabel(sousTitre);
        this.sousTitre.setFont(Theme.police(Font.PLAIN, 13));
        this.sousTitre.setForeground(Theme.TEXTE_SECONDAIRE);
        textes.add(t, BorderLayout.NORTH);
        textes.add(this.sousTitre, BorderLayout.CENTER);
        entete.add(textes, BorderLayout.NORTH);

        barreActions = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        barreActions.setOpaque(false);
        entete.add(barreActions, BorderLayout.CENTER);
        add(entete, BorderLayout.NORTH);
    }

    protected Banque banque() {
        return service.getBanque();
    }

    protected void ajouterAction(JComponent c) {
        barreActions.add(c);
    }

    protected void setSousTitre(String s) {
        sousTitre.setText(s);
    }

    /** Ajuste les marges selon la largeur de la fenetre (appele par la fenetre principale). */
    public void adapterMarges(boolean etroit) {
        setBorder(etroit ? BorderFactory.createEmptyBorder(10, 10, 10, 10)
                : BorderFactory.createEmptyBorder(18, 22, 18, 22));
    }
}
