package vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

import vue.composants.BoutonModerne;
import vue.composants.ChampTexte;
import vue.composants.Formulaire;
import vue.composants.IconeLettre;

/**
 * Ecran d'authentification de l'agent bancaire.
 * Identifiants par defaut : admin / admin
 */
public class FenetreConnexion extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String UTILISATEUR = "admin";
    private static final char[] MOT_DE_PASSE = {'a', 'd', 'm', 'i', 'n'};
    private static final int ESSAIS_MAX = 3;

    private final ChampTexte champUtilisateur = new ChampTexte("Nom d'utilisateur", 18);
    private final JPasswordField champMotDePasse = new JPasswordField(18);
    private final JLabel message = new JLabel(" ");
    private boolean authentifie;
    private int essais;

    public FenetreConnexion(Window parent) {
        super(parent, "Connexion - ASJA Bank", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel racine = new JPanel(new BorderLayout());
        racine.setBackground(Color.WHITE);

        JPanel entete = new JPanel(new BorderLayout(0, 8));
        entete.setBackground(Theme.PRIMAIRE);
        entete.setBorder(BorderFactory.createEmptyBorder(26, 30, 22, 30));
        JLabel logo = new JLabel(new IconeLettre("A", Theme.OR, 56).pleine());
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel titre = new JLabel("ASJA Bank", SwingConstants.CENTER);
        titre.setFont(Theme.police(Font.BOLD, 24));
        titre.setForeground(Color.WHITE);
        JLabel sous = new JLabel("Espace agent - Système bancaire", SwingConstants.CENTER);
        sous.setFont(Theme.police(Font.PLAIN, 13));
        sous.setForeground(new Color(190, 205, 230));
        entete.add(logo, BorderLayout.NORTH);
        entete.add(titre, BorderLayout.CENTER);
        entete.add(sous, BorderLayout.SOUTH);
        racine.add(entete, BorderLayout.NORTH);

        Formulaire f = new Formulaire(260, 1);
        f.setBorder(BorderFactory.createEmptyBorder(22, 30, 6, 30));
        champUtilisateur.setText(UTILISATEUR);
        f.ajouter("Utilisateur", champUtilisateur);
        f.ajouter("Mot de passe", champMotDePasse);
        racine.add(f, BorderLayout.CENTER);

        JPanel bas = new JPanel(new BorderLayout(0, 8));
        bas.setOpaque(false);
        bas.setBorder(BorderFactory.createEmptyBorder(4, 30, 22, 30));
        message.setForeground(Theme.DANGER);
        message.setFont(Theme.police(Font.PLAIN, 12));
        bas.add(message, BorderLayout.NORTH);
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boutons.setOpaque(false);
        BoutonModerne quitter = BoutonModerne.secondaire("Quitter", Theme.TEXTE_SECONDAIRE);
        BoutonModerne connexion = new BoutonModerne("Se connecter", Theme.PRIMAIRE_CLAIR);
        quitter.addActionListener(e -> dispose());
        connexion.addActionListener(e -> verifier());
        boutons.add(quitter);
        boutons.add(connexion);
        bas.add(boutons, BorderLayout.CENTER);
        JLabel aide = new JLabel("Identifiants par défaut : admin / admin");
        aide.setFont(Theme.police(Font.ITALIC, 11));
        aide.setForeground(Theme.TEXTE_SECONDAIRE);
        bas.add(aide, BorderLayout.SOUTH);
        racine.add(bas, BorderLayout.SOUTH);

        setContentPane(racine);
        getRootPane().setDefaultButton(connexion);
        pack();
        setSize(Math.max(getWidth(), 400), getHeight());
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void verifier() {
        char[] saisi = champMotDePasse.getPassword();
        boolean ok = UTILISATEUR.equals(champUtilisateur.getText().trim()) && Arrays.equals(saisi, MOT_DE_PASSE);
        Arrays.fill(saisi, '\0');
        if (ok) {
            authentifie = true;
            dispose();
            return;
        }
        essais++;
        if (essais >= ESSAIS_MAX) {
            Messages.erreur(this, "Nombre maximal de tentatives atteint. L'application va se fermer.");
            dispose();
            return;
        }
        message.setText("Identifiants incorrects (" + (ESSAIS_MAX - essais) + " essai(s) restant(s)).");
        champMotDePasse.setText("");
        champMotDePasse.requestFocusInWindow();
    }

    /** Affiche la fenetre ; retourne true si l'utilisateur est authentifie. */
    public boolean authentifier() {
        javax.swing.SwingUtilities.invokeLater(champMotDePasse::requestFocusInWindow);
        setVisible(true);
        return authentifie;
    }
}
