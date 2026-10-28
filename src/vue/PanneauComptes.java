package vue;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

import exception.BanqueException;
import modele.Client;
import modele.CompteBancaire;
import service.BanqueService;
import util.Formatage;
import vue.composants.BoutonModerne;
import vue.composants.Carte;
import vue.composants.ChampTexte;
import vue.composants.DialogueFormulaire;
import vue.composants.ModeleTable;
import vue.composants.TableModerne;

/**
 * Gestion des comptes bancaires (courants et epargne).
 */
public class PanneauComptes extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final ModeleTable<CompteBancaire> modele;
    private final TableModerne table;
    private final ChampTexte recherche = new ChampTexte("Rechercher (numéro, titulaire...)", 22);
    private final JComboBox<String> filtre = new JComboBox<>(new String[]{
        "Tous les comptes", "Comptes courants", "Comptes épargne", "Comptes débiteurs", "Comptes fermés"});

    public PanneauComptes(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Comptes bancaires", "Ouverture, fermeture et relevés de comptes");

        modele = new ModeleTable<CompteBancaire>(
                new String[]{"Numéro", "Type", "Titulaire", "Catégorie", "Agence", "Solde", "Disponible", "Statut", "Ouverture"},
                new Class<?>[]{String.class, String.class, String.class, String.class, String.class,
                        Double.class, Double.class, String.class, String.class},
                c -> new Object[]{c.getNumeroCompte(), c.getTypeCompte(), c.getTitulaire().getNomComplet(),
                        c.getTitulaire().getCategorie(), c.getAgence() == null ? "-" : c.getAgence().getNomAgence(),
                        c.consulterSolde(), c.getMontantDisponible(), c.isActif() ? "Actif" : "Fermé",
                        Formatage.date(c.getDateOuverture())});
        table = new TableModerne(modele);
        table.setColonnesSecondaires(900, 3, 4, 6, 8);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    releve();
                }
            }
        });

        BoutonModerne ouvrir = new BoutonModerne("+ Ouvrir un compte", Theme.PRIMAIRE_CLAIR);
        BoutonModerne operation = new BoutonModerne("Opérations au guichet", Theme.SUCCES);
        BoutonModerne releve = BoutonModerne.secondaire("Relevé", Theme.INFO);
        BoutonModerne fermer = BoutonModerne.secondaire("Fermer le compte", Theme.DANGER);
        ouvrir.addActionListener(e -> ouvrirCompte());
        operation.addActionListener(e -> {
            CompteBancaire c = selection();
            if (c != null) {
                fenetre.afficherOperations(c);
            }
        });
        releve.addActionListener(e -> releve());
        fermer.addActionListener(e -> fermer());
        ajouterAction(ouvrir);
        ajouterAction(operation);
        ajouterAction(releve);
        ajouterAction(fermer);

        recherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                table.rechercher(recherche.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                table.rechercher(recherche.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                table.rechercher(recherche.getText());
            }
        });
        filtre.addActionListener(e -> appliquerFiltre());

        JPanel ligne = new JPanel(new BorderLayout(10, 0));
        ligne.setOpaque(false);
        ligne.add(recherche, BorderLayout.CENTER);
        ligne.add(filtre, BorderLayout.EAST);
        Carte carte = new Carte(null);
        carte.add(ligne, BorderLayout.NORTH);
        carte.add(table.enDefilement(), BorderLayout.CENTER);
        add(carte, BorderLayout.CENTER);
    }

    private void appliquerFiltre() {
        final int choix = filtre.getSelectedIndex();
        if (choix <= 0) {
            table.setFiltreSupplementaire(null);
            return;
        }
        table.setFiltreSupplementaire(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                CompteBancaire c = modele.getObjet(entry.getIdentifier().intValue());
                switch (choix) {
                    case 1:
                        return c instanceof modele.CompteCourant;
                    case 2:
                        return c instanceof modele.CompteEpargne;
                    case 3:
                        return c.isActif() && c.consulterSolde() < 0;
                    case 4:
                        return !c.isActif();
                    default:
                        return true;
                }
            }
        });
    }

    private CompteBancaire selection() {
        CompteBancaire c = modele.getObjet(table.ligneModeleSelectionnee());
        if (c == null) {
            Messages.info(this, "Sélection", "Veuillez sélectionner un compte dans la liste.");
        }
        return c;
    }

    @Override
    public void rafraichir() {
        CompteBancaire sel = modele.getObjet(table.ligneModeleSelectionnee());
        modele.setDonnees(banque().getTousLesComptes());
        if (sel != null) {
            table.selectionnerLigneModele(modele.indexDe(sel));
        }
        setSousTitre(banque().getNombreComptesActifs() + " compte(s) actif(s) - total des dépôts : "
                + Formatage.monnaie(banque().getTotalDepots()));
    }

    private void releve() {
        CompteBancaire c = selection();
        if (c != null) {
            Messages.texteLong(this, "Relevé du compte " + c.getNumeroCompte(), c.obtenirReleve());
        }
    }

    private void fermer() {
        CompteBancaire c = selection();
        if (c == null) {
            return;
        }
        if (!Messages.confirmerAttention(this, "Fermer le compte",
                "Fermer le compte " + c.getNumeroCompte() + " de " + c.getTitulaire().getNomComplet() + " ?\n"
                        + "Le solde doit être nul et les cartes associées seront bloquées.")) {
            return;
        }
        try {
            service.fermerCompte(c);
            Messages.succes(this, "Le compte " + c.getNumeroCompte() + " a été fermé.");
        } catch (BanqueException ex) {
            Messages.erreur(this, ex.getMessage());
        }
    }

    private void ouvrirCompte() {
        List<Client> clients = banque().getTousLesClients();
        if (clients.isEmpty()) {
            Messages.info(this, "Aucun client", "Créez d'abord un client.");
            return;
        }
        final DialogueFormulaire d = new DialogueFormulaire(this, "Ouvrir un compte", "Ouvrir le compte", 230);
        final JComboBox<Client> client = new JComboBox<>(clients.toArray(new Client[0]));
        final JComboBox<String> type = new JComboBox<>(new String[]{"Compte Courant", "Compte Epargne"});
        final ChampTexte depot = new ChampTexte("0", 12);
        final ChampTexte decouvert = new ChampTexte("Ex : 200 000", 12);
        final ChampTexte frais = new ChampTexte("Ex : 3 000", 12);
        final ChampTexte taux = new ChampTexte("Ex : 4,5", 12);
        final ChampTexte minimum = new ChampTexte("Ex : 50 000", 12);
        decouvert.setText("200000");
        frais.setText("3000");
        taux.setText("4.5");
        minimum.setText("50000");
        depot.setText("0");

        d.getFormulaire().ajouter("Titulaire *", client);
        d.getFormulaire().ajouter("Type de compte *", type);
        d.getFormulaire().ajouter("Dépôt initial (" + Formatage.DEVISE + ")", depot);
        final JPanel cDecouvert = d.getFormulaire().ajouter("Découvert autorisé (" + Formatage.DEVISE + ")", decouvert);
        final JPanel cFrais = d.getFormulaire().ajouter("Frais de gestion / mois", frais);
        final JPanel cTaux = d.getFormulaire().ajouter("Taux d'intérêt annuel (%)", taux);
        final JPanel cMinimum = d.getFormulaire().ajouter("Solde minimum (" + Formatage.DEVISE + ")", minimum);
        cTaux.setVisible(false);
        cMinimum.setVisible(false);

        type.addActionListener(e -> {
            boolean courant = type.getSelectedIndex() == 0;
            cDecouvert.setVisible(courant);
            cFrais.setVisible(courant);
            cTaux.setVisible(!courant);
            cMinimum.setVisible(!courant);
            if (!courant && "0".equals(depot.getText().trim())) {
                depot.setText(minimum.getText());
            }
            d.getFormulaire().revalidate();
        });
        client.addActionListener(e -> {
            Client c = (Client) client.getSelectedItem();
            if (c != null && c.estVIP()) {
                decouvert.setText("2000000");
                frais.setText("0");
            }
        });

        d.setValidation(() -> {
            Client c = (Client) client.getSelectedItem();
            CompteBancaire cpt;
            try {
                double dep = Formatage.lireMontant(depot.getText());
                if (type.getSelectedIndex() == 0) {
                    cpt = service.ouvrirCompteCourant(c, Formatage.lireMontant(decouvert.getText()),
                            Formatage.lireMontant(frais.getText()), dep);
                } else {
                    cpt = service.ouvrirCompteEpargne(c, Formatage.lireMontant(taux.getText()),
                            Formatage.lireMontant(minimum.getText()), dep);
                }
            } catch (NumberFormatException ex) {
                throw new Exception("Veuillez saisir des montants valides.");
            }
            Messages.succes(d, "Compte " + cpt.getNumeroCompte() + " ouvert pour " + c.getNomComplet() + ".");
        });
        d.afficher(720);
    }
}
