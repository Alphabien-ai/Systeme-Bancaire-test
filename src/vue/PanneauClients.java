package vue;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

import modele.Agence;
import modele.CarteBancaire;
import modele.Client;
import modele.ClientVIP;
import modele.CompteBancaire;
import modele.ServiceBancaire;
import service.BanqueService;
import util.Formatage;
import vue.composants.BoutonModerne;
import vue.composants.Carte;
import vue.composants.ChampTexte;
import vue.composants.DialogueFormulaire;
import vue.composants.ModeleTable;
import vue.composants.TableModerne;

/**
 * Gestion des clients (standard et VIP).
 */
public class PanneauClients extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final ModeleTable<Client> modele;
    private final TableModerne table;
    private final ChampTexte recherche = new ChampTexte("Rechercher (nom, id, téléphone...)", 22);
    private final JComboBox<String> filtreCategorie = new JComboBox<>(new String[]{"Tous les clients", "Standard", "VIP"});

    public PanneauClients(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Clients", "Gestion des clients standard et VIP");

        modele = new ModeleTable<Client>(
                new String[]{"ID", "Nom complet", "Catégorie", "Téléphone", "Agence", "Comptes", "Solde total", "Adresse"},
                new Class<?>[]{String.class, String.class, String.class, String.class, String.class, Integer.class,
                        Double.class, String.class},
                c -> new Object[]{c.getIdClient(), c.getNomComplet(), c.getCategorie(), c.getTelephone(),
                        c.getAgence() == null ? "-" : c.getAgence().getNomAgence(), nbComptesActifs(c),
                        c.consulterSolde(), c.getAdresse()});
        table = new TableModerne(modele);
        table.setLargeurColonne(0, 70);
        table.setLargeurColonne(2, 80);
        table.setLargeurColonne(5, 70);
        table.setColonnesSecondaires(820, 3, 4, 5, 7);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    afficherDetails();
                }
            }
        });

        BoutonModerne nouveau = new BoutonModerne("+ Nouveau client", Theme.PRIMAIRE_CLAIR);
        BoutonModerne modifier = BoutonModerne.secondaire("Modifier", Theme.PRIMAIRE_CLAIR);
        BoutonModerne details = BoutonModerne.secondaire("Détails", Theme.INFO);
        BoutonModerne supprimer = BoutonModerne.secondaire("Supprimer", Theme.DANGER);
        nouveau.addActionListener(e -> ouvrirDialogue(null));
        modifier.addActionListener(e -> {
            Client c = selection();
            if (c != null) {
                ouvrirDialogue(c);
            }
        });
        details.addActionListener(e -> afficherDetails());
        supprimer.addActionListener(e -> supprimer());
        ajouterAction(nouveau);
        ajouterAction(modifier);
        ajouterAction(details);
        ajouterAction(supprimer);

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
        filtreCategorie.addActionListener(e -> appliquerFiltreCategorie());

        JPanel ligne = new JPanel(new BorderLayout(10, 0));
        ligne.setOpaque(false);
        ligne.add(recherche, BorderLayout.CENTER);
        ligne.add(filtreCategorie, BorderLayout.EAST);

        Carte carte = new Carte(null);
        carte.add(ligne, BorderLayout.NORTH);
        carte.add(table.enDefilement(), BorderLayout.CENTER);
        add(carte, BorderLayout.CENTER);
    }

    private static int nbComptesActifs(Client c) {
        int n = 0;
        for (CompteBancaire cpt : c.getComptes()) {
            if (cpt.isActif()) {
                n++;
            }
        }
        return n;
    }

    private void appliquerFiltreCategorie() {
        final int choix = filtreCategorie.getSelectedIndex();
        if (choix <= 0) {
            table.setFiltreSupplementaire(null);
            return;
        }
        final String valeur = choix == 1 ? "Standard" : "VIP";
        table.setFiltreSupplementaire(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return valeur.equals(entry.getStringValue(2));
            }
        });
    }

    private Client selection() {
        Client c = modele.getObjet(table.ligneModeleSelectionnee());
        if (c == null) {
            Messages.info(this, "Sélection", "Veuillez sélectionner un client dans la liste.");
        }
        return c;
    }

    @Override
    public void rafraichir() {
        Client sel = modele.getObjet(table.ligneModeleSelectionnee());
        modele.setDonnees(banque().getTousLesClients());
        if (sel != null) {
            table.selectionnerLigneModele(modele.indexDe(sel));
        }
        setSousTitre(banque().getTousLesClients().size() + " client(s) dont " + banque().getNombreVIP() + " VIP");
    }

    // ------------------------------------------------------------ dialogue creation / modification

    private void ouvrirDialogue(final Client existant) {
        boolean creation = existant == null;
        final DialogueFormulaire d = new DialogueFormulaire(this,
                creation ? "Nouveau client" : "Modifier le client " + existant.getIdClient(),
                creation ? "Créer le client" : "Enregistrer", 230);

        final ChampTexte nom = new ChampTexte("Ex : Rakoto", 15);
        final ChampTexte prenom = new ChampTexte("Ex : Jean", 15);
        final ChampTexte tel = new ChampTexte("Ex : 034 12 345 67", 15);
        final ChampTexte adresse = new ChampTexte("Adresse complète", 15);
        final JComboBox<Agence> agence = new JComboBox<>(banque().getAgences().toArray(new Agence[0]));
        final JCheckBox vip = new JCheckBox("Client VIP (retrait au guichet autorisé même si solde insuffisant)");
        vip.setOpaque(false);
        final ChampTexte plafond = new ChampTexte("Ex : 5 000 000", 15);
        final ChampTexte conseiller = new ChampTexte("Nom du conseiller", 15);

        d.getFormulaire().ajouter("Nom *", nom);
        d.getFormulaire().ajouter("Prénom *", prenom);
        d.getFormulaire().ajouter("Téléphone *", tel);
        d.getFormulaire().ajouter("Adresse", adresse);
        d.getFormulaire().ajouter("Agence de rattachement *", agence);
        JPanel cVip = d.getFormulaire().ajouter("Catégorie", vip);
        final JPanel cPlafond = d.getFormulaire().ajouter("Plafond de crédit VIP (" + Formatage.DEVISE + ") *", plafond);
        final JPanel cConseiller = d.getFormulaire().ajouter("Conseiller dédié", conseiller);

        if (creation) {
            plafond.setText("5000000");
            vip.addActionListener(e -> {
                cPlafond.setVisible(vip.isSelected());
                cConseiller.setVisible(vip.isSelected());
                d.getFormulaire().revalidate();
            });
            cPlafond.setVisible(false);
            cConseiller.setVisible(false);
        } else {
            nom.setText(existant.getNom());
            prenom.setText(existant.getPrenom());
            tel.setText(existant.getTelephone());
            adresse.setText(existant.getAdresse());
            agence.setSelectedItem(existant.getAgence());
            cVip.setVisible(false);
            if (existant instanceof ClientVIP) {
                ClientVIP v = (ClientVIP) existant;
                plafond.setText(String.valueOf((long) v.getPlafondCreditVIP()));
                conseiller.setText(v.getConseillerDedie());
            } else {
                cPlafond.setVisible(false);
                cConseiller.setVisible(false);
            }
        }

        d.setValidation(() -> {
            boolean estVip = creation ? vip.isSelected() : existant instanceof ClientVIP;
            double p = 0;
            if (estVip) {
                try {
                    p = Formatage.lireMontant(plafond.getText());
                } catch (NumberFormatException ex) {
                    throw new Exception("Plafond de crédit VIP invalide.");
                }
            }
            Agence a = (Agence) agence.getSelectedItem();
            if (creation) {
                Client c = service.creerClient(nom.getText(), prenom.getText(), adresse.getText(), tel.getText(),
                        a, estVip, p, conseiller.getText());
                Messages.succes(d, "Client " + c.getNomComplet() + " créé (identifiant " + c.getIdClient() + ").");
            } else {
                service.modifierClient(existant, nom.getText(), prenom.getText(), adresse.getText(), tel.getText(),
                        a, p, conseiller.getText());
            }
        });
        d.afficher(720);
    }

    private void supprimer() {
        Client c = selection();
        if (c == null) {
            return;
        }
        if (!Messages.confirmerAttention(this, "Supprimer le client",
                "Supprimer définitivement le client " + c.getNomComplet() + " ?")) {
            return;
        }
        try {
            service.supprimerClient(c);
            Messages.succes(this, "Client supprimé.");
        } catch (exception.BanqueException ex) {
            Messages.erreur(this, ex.getMessage());
        }
    }

    private void afficherDetails() {
        Client c = selection();
        if (c == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FICHE CLIENT\n============================================================\n");
        sb.append("Identifiant : ").append(c.getIdClient()).append('\n');
        sb.append("Nom         : ").append(c.getNomComplet()).append('\n');
        sb.append("Catégorie   : ").append(c.getCategorie()).append('\n');
        sb.append("Téléphone   : ").append(c.getTelephone()).append('\n');
        sb.append("Adresse     : ").append(c.getAdresse()).append('\n');
        sb.append("Agence      : ").append(c.getAgence() == null ? "-" : c.getAgence().getNomAgence()).append('\n');
        if (c instanceof ClientVIP) {
            ClientVIP v = (ClientVIP) c;
            sb.append("Plafond VIP : ").append(Formatage.monnaie(v.getPlafondCreditVIP())).append('\n');
            sb.append("Conseiller  : ").append(v.getConseillerDedie()).append('\n');
            sb.append("Privilège   : retrait direct au guichet même si le solde est insuffisant\n");
        }
        sb.append("Solde total : ").append(Formatage.monnaie(c.consulterSolde())).append('\n');
        sb.append("\nCOMPTES\n------------------------------------------------------------\n");
        for (CompteBancaire cpt : c.getComptes()) {
            sb.append(String.format("  %-10s %-15s %22s  %s%n", cpt.getNumeroCompte(), cpt.getTypeCompte(),
                    Formatage.monnaie(cpt.consulterSolde()), cpt.isActif() ? "Actif" : "Fermé"));
        }
        sb.append("\nCARTES\n------------------------------------------------------------\n");
        for (CarteBancaire k : c.getCartes()) {
            sb.append("  ").append(k.getNumeroMasque()).append("  ").append(k.getTypeCarte())
              .append("  ").append(k.getStatut()).append('\n');
        }
        sb.append("\nSERVICES\n------------------------------------------------------------\n");
        for (ServiceBancaire s : c.getServices()) {
            sb.append("  ").append(s.getNomService()).append(" (").append(Formatage.monnaie(s.getTarifMensuel()))
              .append("/mois)\n");
        }
        Messages.texteLong(this, "Fiche client - " + c.getNomComplet(), sb.toString());
    }
}
