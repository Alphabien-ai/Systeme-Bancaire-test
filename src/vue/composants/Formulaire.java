package vue.composants;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vue.Theme;

/**
 * Formulaire RESPONSIVE : chaque champ (libelle au-dessus) est place dans une
 * GrilleResponsive ; les champs se repartissent sur 1, 2 ou 3 colonnes selon la largeur.
 */
public class Formulaire extends JPanel {
    private static final long serialVersionUID = 1L;

    public Formulaire(int largeurMinChamp) {
        this(largeurMinChamp, Integer.MAX_VALUE);
    }

    public Formulaire(int largeurMinChamp, int maxColonnes) {
        super(new GrilleResponsive(largeurMinChamp, 16, 12, maxColonnes));
        setOpaque(false);
    }

    /** Ajoute un champ libelle ; retourne la cellule (pour pouvoir la masquer). */
    public JPanel ajouter(String libelle, JComponent champ) {
        JPanel cellule = new JPanel(new BorderLayout(0, 5));
        cellule.setOpaque(false);
        JLabel l = new JLabel(libelle);
        l.setFont(Theme.police(Font.BOLD, 12));
        l.setForeground(Theme.TEXTE_SECONDAIRE);
        l.setLabelFor(champ);
        cellule.add(l, BorderLayout.NORTH);
        cellule.add(champ, BorderLayout.CENTER);
        add(cellule);
        return cellule;
    }
}
