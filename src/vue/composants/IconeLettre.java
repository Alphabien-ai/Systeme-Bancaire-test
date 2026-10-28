package vue.composants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import vue.Theme;

/**
 * Icone vectorielle : pastille arrondie contenant 1 ou 2 lettres.
 * Aucun fichier image n'est necessaire et le rendu est net a toute taille.
 */
public class IconeLettre implements Icon {

    private final String texte;
    private final Color couleur;
    private final int taille;
    private boolean pleine = false;

    public IconeLettre(String texte, Color couleur, int taille) {
        this.texte = texte;
        this.couleur = couleur;
        this.taille = taille;
    }

    public IconeLettre pleine() {
        this.pleine = true;
        return this;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (pleine) {
            g2.setColor(couleur);
        } else {
            g2.setColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 38));
        }
        g2.fillRoundRect(x, y, taille, taille, taille / 3, taille / 3);
        g2.setFont(Theme.police(Font.BOLD, taille * 0.38f));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (taille - fm.stringWidth(texte)) / 2;
        int ty = y + (taille - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(pleine ? Color.WHITE : couleur);
        g2.drawString(texte, tx, ty);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return taille;
    }

    @Override
    public int getIconHeight() {
        return taille;
    }
}
