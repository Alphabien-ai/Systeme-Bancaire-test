package vue;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import modele.CompteBancaire;
import service.BanqueService;
import vue.composants.IconeLettre;

/**
 * Fenetre principale de l'application.
 * Disposition RESPONSIVE :
 *  - largeur >= 1100 px : menu lateral etendu ;
 *  - 760 a 1100 px     : menu lateral compact (icones) ;
 *  - largeur < 760 px  : menu lateral masque, navigation par bouton "menu".
 */
public class FenetrePrincipale extends JFrame implements BanqueService.EcouteurDonnees {
    private static final long serialVersionUID = 1L;

    public static final String TABLEAU_BORD = "tableau";
    public static final String CLIENTS = "clients";
    public static final String COMPTES = "comptes";
    public static final String OPERATIONS = "operations";
    public static final String CARTES = "cartes";
    public static final String AGENCES = "agences";
    public static final String SERVICES = "services";
    public static final String RAPPORTS = "rapports";

    private static final int SEUIL_MOBILE = 760;
    private static final int SEUIL_COMPACT = 1100;

    private final BanqueService service;
    private final BarreLaterale barre;
    private final CardLayout cartes = new CardLayout();
    private final JPanel contenu = new JPanel(cartes);
    private final Map<String, PanneauBase> ecrans = new LinkedHashMap<>();
    private final Map<String, String> libelles = new LinkedHashMap<>();
    private final JLabel titrePage = new JLabel();
    private final JLabel date = new JLabel();
    private final JLabel utilisateur = new JLabel("Administrateur");
    private final PanneauOperations panneauOperations;
    private String ecranCourant;
    private Boolean compactForce;
    private boolean modeMobile;

    public FenetrePrincipale(BanqueService service) {
        super("ASJA Bank - Système de gestion bancaire");
        this.service = service;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitter();
            }
        });

        barre = new BarreLaterale(this::afficher);
        panneauOperations = new PanneauOperations(service, this);
        enregistrer(TABLEAU_BORD, "TB", "Tableau de bord", Theme.PRIMAIRE_CLAIR, new PanneauTableauBord(service, this));
        enregistrer(CLIENTS, "CL", "Clients", Theme.VIOLET, new PanneauClients(service, this));
        enregistrer(COMPTES, "CP", "Comptes", Theme.INFO, new PanneauComptes(service, this));
        enregistrer(OPERATIONS, "OP", "Guichet / Opérations", Theme.SUCCES, panneauOperations);
        enregistrer(CARTES, "CB", "Cartes bancaires", new Color(0, 120, 212), new PanneauCartes(service, this));
        enregistrer(AGENCES, "AG", "Agences & Banque", Theme.AVERTISSEMENT, new PanneauAgences(service, this));
        enregistrer(SERVICES, "SV", "Services", new Color(214, 51, 132), new PanneauServices(service, this));
        enregistrer(RAPPORTS, "RP", "Rapports", Theme.OR, new PanneauRapport(service, this));

        JPanel principal = new JPanel(new BorderLayout());
        principal.add(creerEntete(), BorderLayout.NORTH);
        contenu.setBackground(Theme.FOND);
        principal.add(contenu, BorderLayout.CENTER);

        JPanel racine = new JPanel(new BorderLayout());
        racine.add(barre, BorderLayout.WEST);
        racine.add(principal, BorderLayout.CENTER);
        setContentPane(racine);

        installerRaccourcis();
        service.ajouterEcouteur(this);

        Dimension ecran = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1320, (int) (ecran.width * 0.92)), Math.min(840, (int) (ecran.height * 0.9)));
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adapterDisposition();
            }
        });

        new Timer(30_000, e -> majDate()).start();
        majDate();
        adapterDisposition();
        afficher(TABLEAU_BORD);
    }

    private void enregistrer(String cle, String sigle, String libelle, Color couleur, PanneauBase panneau) {
        ecrans.put(cle, panneau);
        libelles.put(cle, libelle);
        contenu.add(panneau, cle);
        barre.ajouter(cle, sigle, libelle, couleur);
    }

    private JComponent creerEntete() {
        JPanel entete = new JPanel(new BorderLayout(10, 0));
        entete.setBackground(Color.WHITE);
        entete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDURE),
                BorderFactory.createEmptyBorder(8, 12, 8, 16)));

        JButton menu = new JButton(new IconeMenu());
        menu.setToolTipText("Menu (Ctrl+M)");
        menu.setFocusPainted(false);
        menu.setContentAreaFilled(false);
        menu.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        menu.addActionListener(e -> basculerMenu(menu));
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "menu");
        getRootPane().getActionMap().put("menu", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                basculerMenu(menu);
            }
        });

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        gauche.setOpaque(false);
        titrePage.setFont(Theme.police(Font.BOLD, 15));
        titrePage.setForeground(Theme.PRIMAIRE);
        gauche.add(menu);
        gauche.add(titrePage);
        entete.add(gauche, BorderLayout.CENTER);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);
        date.setFont(Theme.police(Font.PLAIN, 12));
        date.setForeground(Theme.TEXTE_SECONDAIRE);
        utilisateur.setIcon(new IconeLettre("AD", Theme.PRIMAIRE_CLAIR, 30).pleine());
        utilisateur.setFont(Theme.police(Font.BOLD, 13));
        utilisateur.setForeground(Theme.TEXTE);
        utilisateur.setIconTextGap(8);
        droite.add(date);
        droite.add(utilisateur);
        entete.add(droite, BorderLayout.EAST);
        return entete;
    }

    private void installerRaccourcis() {
        int i = 1;
        for (String cle : ecrans.keySet()) {
            final String c = cle;
            String action = "nav-" + cle;
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, InputEvent.CTRL_DOWN_MASK), action);
            getRootPane().getActionMap().put(action, new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    afficher(c);
                }
            });
            i++;
        }
    }

    private void basculerMenu(Component source) {
        if (modeMobile) {
            JPopupMenu popup = new JPopupMenu();
            for (Map.Entry<String, String> e : libelles.entrySet()) {
                JMenuItem item = new JMenuItem(e.getValue());
                item.setFont(Theme.police(e.getKey().equals(ecranCourant) ? Font.BOLD : Font.PLAIN, 14));
                item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 30));
                final String cle = e.getKey();
                item.addActionListener(ev -> afficher(cle));
                popup.add(item);
            }
            popup.show(source, 0, source.getHeight());
        } else {
            compactForce = !barre.isCompact();
            adapterDisposition();
        }
    }

    /** Coeur du comportement responsive de la fenetre. */
    private void adapterDisposition() {
        int largeur = getContentPane().getWidth();
        if (largeur <= 0) {
            largeur = getWidth();
        }
        modeMobile = largeur < SEUIL_MOBILE;
        barre.setVisible(!modeMobile);
        boolean compact = compactForce != null ? compactForce : largeur < SEUIL_COMPACT;
        barre.setCompact(compact);
        date.setVisible(largeur >= 900);
        utilisateur.setText(largeur >= 600 ? "Administrateur" : "");
        for (PanneauBase p : ecrans.values()) {
            p.adapterMarges(largeur < 600);
        }
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void majDate() {
        date.setText(new SimpleDateFormat("EEEE d MMMM yyyy - HH:mm", Locale.FRANCE).format(new Date()));
    }

    public void afficher(String cle) {
        PanneauBase p = ecrans.get(cle);
        if (p == null) {
            return;
        }
        ecranCourant = cle;
        cartes.show(contenu, cle);
        titrePage.setText(libelles.get(cle));
        barre.selectionner(cle);
        p.rafraichir();
    }

    /** Ouvre le guichet avec un compte preselectionne. */
    public void afficherOperations(CompteBancaire compte) {
        afficher(OPERATIONS);
        panneauOperations.selectionnerCompte(compte);
    }

    @Override
    public void donneesModifiees() {
        if (ecranCourant != null) {
            ecrans.get(ecranCourant).rafraichir();
        }
    }

    /** Tous les ecrans (utilise apres reinitialisation des donnees). */
    public List<PanneauBase> getEcrans() {
        return new ArrayList<>(ecrans.values());
    }

    private void quitter() {
        if (!Messages.confirmer(this, "Quitter", "Voulez-vous vraiment quitter l'application ?\n"
                + "Les données sont sauvegardées automatiquement.")) {
            return;
        }
        try {
            service.sauvegarder();
        } catch (IOException e) {
            if (!Messages.confirmerAttention(this, "Erreur de sauvegarde",
                    "La sauvegarde a échoué : " + e.getMessage() + "\nQuitter quand même ?")) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    /** Icone "hamburger" dessinee (trois traits). */
    private static final class IconeMenu implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMAIRE);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 3; i++) {
                int yy = y + 5 + i * 7;
                g2.drawLine(x + 3, yy, x + 21, yy);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
    }
}
