package vue.composants;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

import vue.Theme;

/**
 * Bouton plat arrondi avec effet de survol (dessine entierement a la main
 * pour un rendu identique sur tous les systemes).
 */
public class BoutonModerne extends JButton {
    private static final long serialVersionUID = 1L;

    private Color fond;
    private Color texte = Color.WHITE;
    private boolean contour;
    private boolean survol;

    public BoutonModerne(String libelle, Color fond) {
        super(libelle);
        this.fond = fond;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setFont(Theme.police(Font.BOLD, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                survol = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                survol = false;
                repaint();
            }
        });
    }

    /** Bouton secondaire : fond blanc, contour et texte colores. */
    public static BoutonModerne secondaire(String libelle, Color couleur) {
        BoutonModerne b = new BoutonModerne(libelle, Color.WHITE);
        b.texte = couleur;
        b.contour = true;
        return b;
    }

    @Override
    public void updateUI() {
        setUI(new BasicButtonUI());
    }

    public void setFond(Color fond) {
        this.fond = fond;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int l = fm.stringWidth(getText() == null ? "" : getText()) + 32;
        return new Dimension(Math.max(l, 80), fm.getHeight() + 18);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color c;
        Color t = texte;
        if (!isEnabled()) {
            c = contour ? Color.WHITE : Theme.DESACTIVE;
            t = contour ? Theme.DESACTIVE : Color.WHITE;
        } else if (getModel().isPressed()) {
            c = contour ? Theme.SELECTION : fond.darker();
        } else if (survol) {
            c = contour ? Theme.LIGNE_ALTERNEE : eclaircir(fond);
        } else {
            c = fond;
        }
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        if (contour) {
            g2.setColor(isEnabled() ? texte : Theme.DESACTIVE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        }
        if (isFocusOwner()) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
        }
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String s = getText() == null ? "" : getText();
        int x = (getWidth() - fm.stringWidth(s)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(t);
        g2.drawString(s, x, y);
        g2.dispose();
    }

    private static Color eclaircir(Color c) {
        int r = Math.min(255, c.getRed() + 22);
        int v = Math.min(255, c.getGreen() + 22);
        int b = Math.min(255, c.getBlue() + 22);
        return new Color(r, v, b);
    }
}
