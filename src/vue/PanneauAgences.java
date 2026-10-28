package vue;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import exception.BanqueException;
import modele.Agence;
import modele.Banque;
import modele.Client;
import service.BanqueService;
import util.Formatage;
import vue.composants.BoutonModerne;
import vue.composants.Carte;
import vue.composants.ChampTexte;
import vue.composants.DialogueFormulaire;
import vue.composants.GrilleResponsive;
import vue.composants.ModeleTable;
import vue.composants.TableModerne;

/**
 * Informations de la banque et gestion des agences.
 */
public class PanneauAgences extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final ModeleTable<Agence> modele;
    private final TableModerne table;
    private final JLabel lblNom = info();
    private final JLabel lblId = info();
    private final JLabel lblAdresse = info();
    private final JLabel lblCapital = info();

    public PanneauAgences(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Agences & Banque", "Informations de la banque et réseau d'agences");

        modele = new ModeleTable<Agence>(
                new String[]{"Code", "Agence", "Adresse", "Téléphone", "Clients", "Comptes", "Dépôts"},
                new Class<?>[]{String.class, String.class, String.class, String.class, Integer.class, Integer.class, Double.class},
                a -> new Object[]{a.getCodeAgence(), a.getNomAgence(), a.getAdresse(), a.getTelephone(),
                        a.getClients().size(), a.getComptes().size(), a.getTotalDepots()});
        table = new TableModerne(modele);
        table.setColonnesSecondaires(820, 2, 3, 5);

        BoutonModerne nouvelle = new BoutonModerne("+ Nouvelle agence", Theme.PRIMAIRE_CLAIR);
        BoutonModerne modifier = BoutonModerne.secondaire("Modifier l'agence", Theme.PRIMAIRE_CLAIR);
        BoutonModerne clients = BoutonModerne.secondaire("Clients de l'agence", Theme.INFO);
        BoutonModerne banqueBtn = BoutonModerne.secondaire("Modifier la banque", Theme.AVERTISSEMENT);
        nouvelle.addActionListener(e -> dialogueAgence(null));
        modifier.addActionListener(e -> {
            Agence a = selection();
            if (a != null) {
                dialogueAgence(a);
            }
        });
        clients.addActionListener(e -> listerClients());
        banqueBtn.addActionListener(e -> dialogueBanque());
        ajouterAction(nouvelle);
        ajouterAction(modifier);
        ajouterAction(clients);
        ajouterAction(banqueBtn);

        JPanel infos = new JPanel(new GrilleResponsive(200, 16, 10));
        infos.setOpaque(false);
        infos.add(bloc("Banque", lblNom));
        infos.add(bloc("Identifiant", lblId));
        infos.add(bloc("Siège", lblAdresse));
        infos.add(bloc("Capital", lblCapital));
        Carte carteBanque = new Carte("Informations de la banque", infos);
        carteBanque.setCouleurBordure(Theme.PRIMAIRE_CLAIR);

        Carte carteTable = new Carte("Réseau d'agences", table.enDefilement());

        JPanel centre = new JPanel(new BorderLayout(0, 14));
        centre.setOpaque(false);
        centre.add(carteBanque, BorderLayout.NORTH);
        centre.add(carteTable, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);
    }

    private static JLabel info() {
        JLabel l = new JLabel("-");
        l.setFont(Theme.police(Font.BOLD, 14));
        l.setForeground(Theme.TEXTE);
        return l;
    }

    private static JPanel bloc(String titre, JLabel valeur) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel t = new JLabel(titre.toUpperCase());
        t.setFont(Theme.police(Font.BOLD, 11));
        t.setForeground(Theme.TEXTE_SECONDAIRE);
        p.add(t, BorderLayout.NORTH);
        p.add(valeur, BorderLayout.CENTER);
        return p;
    }

    private Agence selection() {
        Agence a = modele.getObjet(table.ligneModeleSelectionnee());
        if (a == null) {
            Messages.info(this, "Sélection", "Veuillez sélectionner une agence.");
        }
        return a;
    }

    @Override
    public void rafraichir() {
        Banque b = banque();
        lblNom.setText(b.getNom());
        lblId.setText(b.getIdBanque());
        lblAdresse.setText(b.getAdresse());
        lblAdresse.setToolTipText(b.getAdresse());
        lblCapital.setText(Formatage.monnaie(b.getCapital()));
        Agence sel = modele.getObjet(table.ligneModeleSelectionnee());
        modele.setDonnees(b.getAgences());
        if (sel != null) {
            table.selectionnerLigneModele(modele.indexDe(sel));
        }
        setSousTitre(b.getAgences().size() + " agence(s)");
    }

    private void dialogueAgence(final Agence existante) {
        final boolean creation = existante == null;
        final DialogueFormulaire d = new DialogueFormulaire(this, creation ? "Nouvelle agence" : "Modifier l'agence",
                creation ? "Créer" : "Enregistrer", 240);
        final ChampTexte nom = new ChampTexte("Ex : Agence Mahajanga", 15);
        final ChampTexte adresse = new ChampTexte("Adresse", 15);
        final ChampTexte tel = new ChampTexte("Téléphone", 15);
        if (!creation) {
            nom.setText(existante.getNomAgence());
            adresse.setText(existante.getAdresse());
            tel.setText(existante.getTelephone());
        }
        d.getFormulaire().ajouter("Nom de l'agence *", nom);
        d.getFormulaire().ajouter("Adresse", adresse);
        d.getFormulaire().ajouter("Téléphone", tel);
        d.setValidation(() -> {
            if (creation) {
                service.creerAgence(nom.getText(), adresse.getText(), tel.getText());
            } else {
                service.modifierAgence(existante, nom.getText(), adresse.getText(), tel.getText());
            }
        });
        d.afficher(640);
    }

    private void dialogueBanque() {
        final Banque b = banque();
        final DialogueFormulaire d = new DialogueFormulaire(this, "Modifier la banque", "Enregistrer", 240);
        final ChampTexte nom = new ChampTexte("Nom", 15);
        final ChampTexte adresse = new ChampTexte("Adresse du siège", 15);
        final ChampTexte capital = new ChampTexte("Capital", 15);
        nom.setText(b.getNom());
        adresse.setText(b.getAdresse());
        capital.setText(String.valueOf((long) b.getCapital()));
        d.getFormulaire().ajouter("Nom de la banque *", nom);
        d.getFormulaire().ajouter("Siège", adresse);
        d.getFormulaire().ajouter("Capital (" + Formatage.DEVISE + ")", capital);
        d.setValidation(() -> {
            double cap;
            try {
                cap = Formatage.lireMontant(capital.getText());
            } catch (NumberFormatException ex) {
                throw new BanqueException("Capital invalide.");
            }
            service.modifierBanque(nom.getText(), adresse.getText(), cap);
        });
        d.afficher(640);
    }

    private void listerClients() {
        Agence a = selection();
        if (a == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CLIENTS DE L'AGENCE ").append(a.getNomAgence().toUpperCase()).append("\n");
        sb.append("============================================================\n");
        for (Client c : a.getClients()) {
            sb.append(String.format("  %-8s %-30s %-9s %22s%n", c.getIdClient(), c.getNomComplet(), c.getCategorie(),
                    Formatage.monnaie(c.consulterSolde())));
        }
        if (a.getClients().isEmpty()) {
            sb.append("  Aucun client.\n");
        }
        Messages.texteLong(this, "Clients - " + a.getNomAgence(), sb.toString());
    }
}
