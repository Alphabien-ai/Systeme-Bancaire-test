package vue;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import exception.BanqueException;
import modele.CarteBancaire;
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
 * Gestion des cartes bancaires : demande, activation, blocage, consultation du solde.
 */
public class PanneauCartes extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final ModeleTable<CarteBancaire> modele;
    private final TableModerne table;
    private final ChampTexte recherche = new ChampTexte("Rechercher une carte (titulaire, compte, type...)", 22);

    public PanneauCartes(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Cartes bancaires", "Demande, activation et blocage des cartes");

        modele = new ModeleTable<CarteBancaire>(
                new String[]{"Numéro", "Type", "Titulaire", "Compte", "Expiration", "Statut"},
                new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class},
                k -> new Object[]{k.getNumeroMasque(), k.getTypeCarte(), k.getCompte().getTitulaire().getNomComplet(),
                        k.getCompte().getNumeroCompte(), Formatage.date(k.getDateExpiration()), k.getStatut()});
        table = new TableModerne(modele);
        table.setColonnesSecondaires(760, 3, 4);

        BoutonModerne nouvelle = new BoutonModerne("+ Demander une carte", Theme.PRIMAIRE_CLAIR);
        BoutonModerne activer = BoutonModerne.secondaire("Activer", Theme.SUCCES);
        BoutonModerne bloquer = BoutonModerne.secondaire("Bloquer", Theme.DANGER);
        BoutonModerne solde = BoutonModerne.secondaire("Vérifier le solde", Theme.INFO);
        nouvelle.addActionListener(e -> demanderCarte());
        activer.addActionListener(e -> {
            CarteBancaire k = selection();
            if (k != null) {
                try {
                    service.activerCarte(k);
                    Messages.succes(this, "Carte " + k.getNumeroMasque() + " activée.");
                } catch (BanqueException ex) {
                    Messages.erreur(this, ex.getMessage());
                }
            }
        });
        bloquer.addActionListener(e -> {
            CarteBancaire k = selection();
            if (k != null && Messages.confirmerAttention(this, "Bloquer la carte",
                    "Bloquer la carte " + k.getNumeroMasque() + " ?")) {
                service.bloquerCarte(k);
                Messages.succes(this, "Carte bloquée.");
            }
        });
        solde.addActionListener(e -> {
            CarteBancaire k = selection();
            if (k != null) {
                try {
                    Messages.info(this, "Solde", "Carte " + k.getNumeroMasque() + "\nCompte "
                            + k.getCompte().getNumeroCompte() + "\nSolde : " + Formatage.monnaie(k.verifierSolde()));
                } catch (IllegalStateException ex) {
                    Messages.erreur(this, ex.getMessage());
                }
            }
        });
        ajouterAction(nouvelle);
        ajouterAction(activer);
        ajouterAction(bloquer);
        ajouterAction(solde);

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

        Carte carte = new Carte(null);
        carte.add(recherche, BorderLayout.NORTH);
        carte.add(table.enDefilement(), BorderLayout.CENTER);
        add(carte, BorderLayout.CENTER);
    }

    private CarteBancaire selection() {
        CarteBancaire k = modele.getObjet(table.ligneModeleSelectionnee());
        if (k == null) {
            Messages.info(this, "Sélection", "Veuillez sélectionner une carte.");
        }
        return k;
    }

    @Override
    public void rafraichir() {
        CarteBancaire sel = modele.getObjet(table.ligneModeleSelectionnee());
        List<CarteBancaire> cartes = banque().getToutesLesCartes();
        modele.setDonnees(cartes);
        if (sel != null) {
            table.selectionnerLigneModele(modele.indexDe(sel));
        }
        setSousTitre(cartes.size() + " carte(s) émise(s)");
    }

    private void demanderCarte() {
        List<CompteBancaire> comptes = banque().getComptesActifs();
        if (comptes.isEmpty()) {
            Messages.info(this, "Aucun compte", "Aucun compte actif.");
            return;
        }
        final DialogueFormulaire d = new DialogueFormulaire(this, "Demander une carte bancaire", "Émettre la carte", 240);
        final JComboBox<CompteBancaire> compte = new JComboBox<>(comptes.toArray(new CompteBancaire[0]));
        final JComboBox<String> type = new JComboBox<>(CarteBancaire.TYPES);
        d.getFormulaire().ajouter("Compte lié *", compte);
        d.getFormulaire().ajouter("Type de carte *", type);
        d.setValidation(() -> {
            CarteBancaire k = service.demanderCarte((CompteBancaire) compte.getSelectedItem(),
                    (String) type.getSelectedItem());
            Messages.succes(d, "Carte " + k.getNumeroMasque() + " émise (statut : " + k.getStatut()
                    + ").\nPensez à l'activer.");
        });
        d.afficher(620);
    }
}
