package vue.composants;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

import vue.Theme;

/**
 * Champ de saisie avec texte indicatif (placeholder).
 */
public class ChampTexte extends JTextField {
    private static final long serialVersionUID = 1L;
    private final String indication;

    public ChampTexte(String indication, int colonnes) {
        super(colonnes);
        this.indication = indication;
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && indication != null && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.TEXTE_SECONDAIRE);
            g2.setFont(getFont().deriveFont(java.awt.Font.ITALIC));
            Insets in = getInsets();
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(indication, in.left + 2, y);
            g2.dispose();
        }
    }
}
