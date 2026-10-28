package vue.composants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vue.Theme;

/**
 * Indicateur cle (KPI) du tableau de bord.
 */
public class CarteStatistique extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Color accent;
    private final JLabel valeur = new JLabel("-");
    private final JLabel detail = new JLabel(" ");

    public CarteStatistique(String titre, String symbole, Color accent) {
        super(new BorderLayout(12, 0));
        this.accent = accent;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));

        JLabel icone = new JLabel(new IconeLettre(symbole, accent, 44));
        add(icone, BorderLayout.WEST);

        JPanel textes = new JPanel(new GridLayout(3, 1, 0, 2));
        textes.setOpaque(false);
        JLabel t = new JLabel(titre.toUpperCase());
        t.setFont(Theme.police(Font.BOLD, 11));
        t.setForeground(Theme.TEXTE_SECONDAIRE);
        valeur.setFont(Theme.police(Font.BOLD, 20));
        valeur.setForeground(Theme.TEXTE);
        detail.setFont(Theme.police(Font.PLAIN, 12));
        detail.setForeground(Theme.TEXTE_SECONDAIRE);
        textes.add(t);
        textes.add(valeur);
        textes.add(detail);
        add(textes, BorderLayout.CENTER);
    }

    public void setValeur(String v, String d) {
        valeur.setText(v);
        valeur.setToolTipText(v);
        detail.setText(d == null ? " " : d);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 96));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 14, 14);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 14, 14);
        g2.setColor(Theme.BORDURE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 14, 14);
        g2.setColor(accent);
        g2.fillRoundRect(0, 10, 5, getHeight() - 22, 5, 5);
        g2.dispose();
        super.paintComponent(g);
    }
}
