package vue;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;

import exception.BanqueException;
import modele.Client;
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
 * Catalogue des services bancaires et souscriptions des clients.
 */
public class PanneauServices extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final ModeleTable<ServiceBancaire> modele;
    private final TableModerne table;

    public PanneauServices(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Services bancaires", "Catalogue des services et souscriptions");

        modele = new ModeleTable<ServiceBancaire>(
                new String[]{"Code", "Service", "Description", "Tarif / mois", "Souscripteurs"},
                new Class<?>[]{String.class, String.class, String.class, Double.class, Integer.class},
                s -> new Object[]{s.getCodeService(), s.getNomService(), s.getDescription(), s.getTarifMensuel(),
                        s.getSouscripteurs().size()});
        table = new TableModerne(modele);
        table.setLargeurColonne(0, 60);
        table.setColonnesSecondaires(760, 0, 2);

        BoutonModerne nouveau = new BoutonModerne("+ Nouveau service", Theme.PRIMAIRE_CLAIR);
        BoutonModerne souscrire = BoutonModerne.secondaire("Souscrire un client", Theme.SUCCES);
        BoutonModerne resilier = BoutonModerne.secondaire("Résilier", Theme.DANGER);
        BoutonModerne consulter = BoutonModerne.secondaire("Consulter", Theme.INFO);
        nouveau.addActionListener(e -> nouveauService());
        souscrire.addActionListener(e -> gererSouscription(true));
        resilier.addActionListener(e -> gererSouscription(false));
        consulter.addActionListener(e -> {
            ServiceBancaire s = selection();
            if (s != null) {
                Messages.texteLong(this, "Service " + s.getNomService(), s.consulter());
            }
        });
        ajouterAction(nouveau);
        ajouterAction(souscrire);
        ajouterAction(resilier);
        ajouterAction(consulter);

        add(new Carte(null, table.enDefilement()), BorderLayout.CENTER);
    }

    private ServiceBancaire selection() {
        ServiceBancaire s = modele.getObjet(table.ligneModeleSelectionnee());
        if (s == null) {
            Messages.info(this, "Sélection", "Veuillez sélectionner un service.");
        }
        return s;
    }

    @Override
    public void rafraichir() {
        ServiceBancaire sel = modele.getObjet(table.ligneModeleSelectionnee());
        modele.setDonnees(banque().getServices());
        if (sel != null) {
            table.selectionnerLigneModele(modele.indexDe(sel));
        }
        setSousTitre(banque().getServices().size() + " service(s) proposé(s)");
    }

    private void nouveauService() {
        final DialogueFormulaire d = new DialogueFormulaire(this, "Nouveau service", "Créer", 240);
        final ChampTexte nom = new ChampTexte("Ex : Mobile Banking", 15);
        final ChampTexte desc = new ChampTexte("Description", 15);
        final ChampTexte tarif = new ChampTexte("Ex : 3 000", 15);
        d.getFormulaire().ajouter("Nom du service *", nom);
        d.getFormulaire().ajouter("Description", desc);
        d.getFormulaire().ajouter("Tarif mensuel (" + Formatage.DEVISE + ")", tarif);
        d.setValidation(() -> {
            double t;
            try {
                t = tarif.getText().trim().isEmpty() ? 0 : Formatage.lireMontant(tarif.getText());
            } catch (NumberFormatException ex) {
                throw new BanqueException("Tarif invalide.");
            }
            service.creerService(nom.getText(), desc.getText(), t);
        });
        d.afficher(640);
    }

    private void gererSouscription(final boolean souscrire) {
        final ServiceBancaire s = selection();
        if (s == null) {
            return;
        }
        List<Client> candidats = souscrire ? banque().getTousLesClients() : s.getSouscripteurs();
        if (candidats.isEmpty()) {
            Messages.info(this, "Aucun client", souscrire ? "Aucun client disponible." : "Aucun souscripteur.");
            return;
        }
        final DialogueFormulaire d = new DialogueFormulaire(this,
                (souscrire ? "Souscrire au service " : "Résilier le service ") + s.getNomService(),
                souscrire ? "Souscrire" : "Résilier", 260);
        final JComboBox<Client> client = new JComboBox<>(candidats.toArray(new Client[0]));
        d.getFormulaire().ajouter("Client *", client);
        d.setValidation(() -> {
            Client c = (Client) client.getSelectedItem();
            if (souscrire) {
                service.souscrire(s, c);
            } else {
                service.resilier(s, c);
            }
            Messages.succes(d, (souscrire ? "Souscription" : "Résiliation") + " enregistrée pour " + c.getNomComplet() + ".");
        });
        d.afficher(560);
    }
}
