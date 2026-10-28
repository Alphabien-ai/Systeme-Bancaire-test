package vue;

import java.awt.BorderLayout;
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

import vue.composants.IconeLettre;

/**
 * Menu lateral de navigation. Il possede deux modes :
 * etendu (icone + libelle) et compact (icones seules) pour les fenetres etroites.
 */
public class BarreLaterale extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int LARGEUR_ETENDUE = 236;
    public static final int LARGEUR_COMPACTE = 74;

    /** Notifie la cle de l'ecran demande. */
    public interface EcouteurNavigation {
        void naviguer(String cle);
    }

    private final EcouteurNavigation ecouteur;
    private final List<ElementNavigation> elements = new ArrayList<>();
    private final JPanel liste = new JPanel();
    private final JLabel nomBanque = new JLabel("ASJA Bank");
    private final JLabel sousTitre = new JLabel("Système bancaire");
    private final JLabel pied = new JLabel("v1.0 - Java Swing");
    private boolean compact;

    public BarreLaterale(EcouteurNavigation ecouteur) {
        super(new BorderLayout());
        this.ecouteur = ecouteur;
        setBackground(Theme.BARRE_LATERALE);

        JPanel entete = new JPanel(new BorderLayout(10, 0));
        entete.setOpaque(false);
        entete.setBorder(BorderFactory.createEmptyBorder(20, 16, 22, 10));
        JLabel logo = new JLabel(new IconeLettre("A", Theme.OR, 40).pleine());
        entete.add(logo, BorderLayout.WEST);
        JPanel textes = new JPanel(new BorderLayout());
        textes.setOpaque(false);
        nomBanque.setFont(Theme.police(Font.BOLD, 17));
        nomBanque.setForeground(Color.WHITE);
        sousTitre.setFont(Theme.police(Font.PLAIN, 12));
        sousTitre.setForeground(new Color(160, 176, 205));
        textes.add(nomBanque, BorderLayout.CENTER);
        textes.add(sousTitre, BorderLayout.SOUTH);
        entete.add(textes, BorderLayout.CENTER);
        add(entete, BorderLayout.NORTH);

        liste.setLayout(new BoxLayout(liste, BoxLayout.Y_AXIS));
        liste.setOpaque(false);
        liste.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(liste, BorderLayout.CENTER);

        pied.setFont(Theme.police(Font.PLAIN, 11));
        pied.setForeground(new Color(120, 136, 165));
        pied.setBorder(BorderFactory.createEmptyBorder(10, 18, 14, 10));
        add(pied, BorderLayout.SOUTH);
    }

    public void ajouter(String cle, String sigle, String libelle, Color couleur) {
        ElementNavigation e = new ElementNavigation(cle, sigle, libelle, couleur);
        elements.add(e);
        liste.add(e);
        liste.add(Box.createVerticalStrut(4));
    }

    public void selectionner(String cle) {
        for (ElementNavigation e : elements) {
            e.selectionne = e.cle.equals(cle);
            e.repaint();
        }
    }

    public void setCompact(boolean compact) {
        if (this.compact == compact) {
            return;
        }
        this.compact = compact;
        nomBanque.setVisible(!compact);
        sousTitre.setVisible(!compact);
        pied.setText(compact ? "v1.0" : "v1.0 - Java Swing");
        for (ElementNavigation e : elements) {
            e.setToolTipText(compact ? e.libelle : null);
        }
        revalidate();
        repaint();
    }

    public boolean isCompact() {
        return compact;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(compact ? LARGEUR_COMPACTE : LARGEUR_ETENDUE, super.getPreferredSize().height);
    }

    /** Bouton de navigation dessine sur mesure. */
    private final class ElementNavigation extends JButton {
        private static final long serialVersionUID = 1L;
        private final String cle;
        private final String libelle;
        private final IconeLettre icone;
        private boolean selectionne;
        private boolean survol;

        ElementNavigation(String cle, String sigle, String libelle, Color couleur) {
            this.cle = cle;
            this.libelle = libelle;
            this.icone = new IconeLettre(sigle, couleur, 32).pleine();
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);
            getAccessibleContext().setAccessibleName(libelle);
            addActionListener(e -> ecouteur.naviguer(this.cle));
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

        @Override
        public void updateUI() {
            setUI(new BasicButtonUI());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(compact ? 54 : 210, 46);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 46);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (selectionne || survol || isFocusOwner()) {
                g2.setColor(selectionne ? Theme.BARRE_SURVOL : new Color(255, 255, 255, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            if (selectionne) {
                g2.setColor(Theme.OR);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            }
            int xIcone = compact ? (getWidth() - icone.getIconWidth()) / 2 : 12;
            icone.paintIcon(this, g2, xIcone, (getHeight() - icone.getIconHeight()) / 2);
            if (!compact) {
                g2.setFont(Theme.police(selectionne ? Font.BOLD : Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(selectionne ? Color.WHITE : new Color(205, 214, 230));
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(libelle, 56, y);
            }
            g2.dispose();
        }
    }
}
