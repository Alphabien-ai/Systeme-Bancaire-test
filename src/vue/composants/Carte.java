package vue.composants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vue.Theme;

/**
 * Conteneur blanc arrondi avec titre optionnel (style "card").
 */
public class Carte extends JPanel {
    private static final long serialVersionUID = 1L;
    private Color couleurBordure = Theme.BORDURE;
    private Color fond = Theme.CARTE;

    public Carte(String titre) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        if (titre != null) {
            JLabel l = new JLabel(titre);
            l.setFont(Theme.police(Font.BOLD, 15));
            l.setForeground(Theme.TEXTE);
            add(l, BorderLayout.NORTH);
        }
    }

    public Carte(String titre, JComponent contenu) {
        this(titre);
        add(contenu, BorderLayout.CENTER);
    }

    public void setCouleurBordure(Color c) {
        this.couleurBordure = c;
        repaint();
    }

    public void setFond(Color c) {
        this.fond = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 14, 14);
        g2.setColor(fond);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 14, 14);
        g2.setColor(couleurBordure);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}
