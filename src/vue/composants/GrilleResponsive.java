package vue.composants;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Gestionnaire de mise en page RESPONSIVE :
 * le nombre de colonnes est calcule automatiquement selon la largeur
 * disponible et la largeur minimale d'une cellule (comme une grille CSS
 * "auto-fit / minmax"). Les composants passent a la ligne quand l'ecran retrecit.
 */
public class GrilleResponsive implements LayoutManager {

    private final int largeurMin;
    private final int hgap;
    private final int vgap;
    private final int maxColonnes;
    private boolean etirerDerniereLigne;
    private int derniereLargeurCalculee = -1;

    public GrilleResponsive(int largeurMin, int hgap, int vgap) {
        this(largeurMin, hgap, vgap, Integer.MAX_VALUE);
    }

    public GrilleResponsive(int largeurMin, int hgap, int vgap, int maxColonnes) {
        this.largeurMin = Math.max(50, largeurMin);
        this.hgap = hgap;
        this.vgap = vgap;
        this.maxColonnes = Math.max(1, maxColonnes);
    }

    /** Si vrai, la derniere ligne occupe toute la hauteur restante. */
    public GrilleResponsive setEtirerDerniereLigne(boolean etirer) {
        this.etirerDerniereLigne = etirer;
        return this;
    }

    public int calculerColonnes(int largeurDisponible, int nbComposants) {
        if (nbComposants <= 0) {
            return 1;
        }
        int c = (largeurDisponible + hgap) / (largeurMin + hgap);
        return Math.max(1, Math.min(c, Math.min(nbComposants, maxColonnes)));
    }

    private List<Component> visibles(Container parent) {
        List<Component> liste = new ArrayList<>();
        for (Component c : parent.getComponents()) {
            if (c.isVisible()) {
                liste.add(c);
            }
        }
        return liste;
    }

    private int[] hauteursLignes(List<Component> comps, int colonnes) {
        int lignes = (comps.size() + colonnes - 1) / colonnes;
        int[] h = new int[Math.max(lignes, 0)];
        for (int i = 0; i < comps.size(); i++) {
            int l = i / colonnes;
            h[l] = Math.max(h[l], comps.get(i).getPreferredSize().height);
        }
        return h;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets in = parent.getInsets();
            List<Component> comps = visibles(parent);
            int largeur = parent.getWidth();
            if (largeur <= 0) {
                int estimees = Math.min(Math.max(1, comps.size()), maxColonnes == Integer.MAX_VALUE ? 3 : maxColonnes);
                largeur = in.left + in.right + estimees * (largeurMin + hgap);
            }
            derniereLargeurCalculee = parent.getWidth();
            int dispo = Math.max(0, largeur - in.left - in.right);
            int colonnes = calculerColonnes(dispo, comps.size());
            int[] h = hauteursLignes(comps, colonnes);
            int total = 0;
            for (int i = 0; i < h.length; i++) {
                total += h[i];
                if (i > 0) {
                    total += vgap;
                }
            }
            return new Dimension(largeur, total + in.top + in.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets in = parent.getInsets();
        return new Dimension(largeurMin + in.left + in.right, in.top + in.bottom);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets in = parent.getInsets();
            List<Component> comps = visibles(parent);
            if (comps.isEmpty()) {
                return;
            }
            int dispo = Math.max(0, parent.getWidth() - in.left - in.right);
            int colonnes = calculerColonnes(dispo, comps.size());
            int largeurCellule = Math.max(0, (dispo - (colonnes - 1) * hgap) / colonnes);
            int[] h = hauteursLignes(comps, colonnes);

            if (etirerDerniereLigne && h.length > 0) {
                int utilise = 0;
                for (int i = 0; i < h.length; i++) {
                    utilise += h[i] + (i > 0 ? vgap : 0);
                }
                int reste = parent.getHeight() - in.top - in.bottom - utilise;
                if (reste > 0) {
                    h[h.length - 1] += reste;
                }
            }

            int y = in.top;
            for (int i = 0; i < comps.size(); i++) {
                int ligne = i / colonnes;
                int col = i % colonnes;
                if (col == 0 && ligne > 0) {
                    y += h[ligne - 1] + vgap;
                }
                int x = in.left + col * (largeurCellule + hgap);
                comps.get(i).setBounds(x, y, largeurCellule, h[ligne]);
            }

            // La hauteur preferee depend de la largeur : si la largeur a change
            // depuis le dernier calcul, on redemande une mise en page.
            if (derniereLargeurCalculee != parent.getWidth()) {
                derniereLargeurCalculee = parent.getWidth();
                SwingUtilities.invokeLater(parent::revalidate);
            }
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
}
