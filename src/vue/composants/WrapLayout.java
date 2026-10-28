package vue.composants;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout qui calcule correctement sa hauteur lorsque les composants
 * passent a la ligne (barres d'outils responsives).
 */
public class WrapLayout extends FlowLayout {
    private static final long serialVersionUID = 1L;
    private int derniereLargeur = -1;

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return taille(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension min = taille(target, false);
        min.width -= (getHgap() + 1);
        return min;
    }

    @Override
    public void layoutContainer(Container target) {
        super.layoutContainer(target);
        if (derniereLargeur != target.getWidth()) {
            derniereLargeur = target.getWidth();
            SwingUtilities.invokeLater(target::revalidate);
        }
    }

    private Dimension taille(Container target, boolean preferee) {
        synchronized (target.getTreeLock()) {
            Container conteneur = target;
            while (conteneur.getSize().width == 0 && conteneur.getParent() != null) {
                conteneur = conteneur.getParent();
            }
            int largeurCible = conteneur.getSize().width;
            if (largeurCible == 0) {
                largeurCible = Integer.MAX_VALUE;
            }
            derniereLargeur = target.getWidth();
            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int margesH = insets.left + insets.right + (hgap * 2);
            int largeurMax = largeurCible - margesH;

            Dimension dim = new Dimension(0, 0);
            int largeurLigne = 0;
            int hauteurLigne = 0;
            for (Component m : target.getComponents()) {
                if (!m.isVisible()) {
                    continue;
                }
                Dimension d = preferee ? m.getPreferredSize() : m.getMinimumSize();
                if (largeurLigne + d.width > largeurMax && largeurLigne > 0) {
                    ajouterLigne(dim, largeurLigne, hauteurLigne);
                    largeurLigne = 0;
                    hauteurLigne = 0;
                }
                if (largeurLigne != 0) {
                    largeurLigne += hgap;
                }
                largeurLigne += d.width;
                hauteurLigne = Math.max(hauteurLigne, d.height);
            }
            ajouterLigne(dim, largeurLigne, hauteurLigne);
            dim.width += margesH;
            dim.height += insets.top + insets.bottom + vgap * 2;
            Container sp = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (sp != null && target.isValid()) {
                dim.width -= (hgap + 1);
            }
            return dim;
        }
    }

    private void ajouterLigne(Dimension dim, int largeurLigne, int hauteurLigne) {
        dim.width = Math.max(dim.width, largeurLigne);
        if (dim.height > 0) {
            dim.height += getVgap();
        }
        dim.height += hauteurLigne;
    }
}
