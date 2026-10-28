package vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import exception.BanqueException;
import exception.SoldeInsuffisantException;
import modele.ClientVIP;
import modele.CompteBancaire;
import modele.OperationBancaire;
import service.BanqueService;
import util.Formatage;
import vue.composants.BoutonModerne;
import vue.composants.Carte;
import vue.composants.ChampTexte;
import vue.composants.Formulaire;
import vue.composants.GrilleResponsive;
import vue.composants.ModeleTable;
import vue.composants.PanneauDefilant;
import vue.composants.TableModerne;
import vue.composants.WrapLayout;

/**
 * Guichet : depots, retraits, virements, et RETRAIT DIRECT AU GUICHET pour
 * les clients VIP (autorise meme si le solde est insuffisant ou negatif).
 */
public class PanneauOperations extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final ChampTexte rechercheCompte = new ChampTexte("Filtrer : numéro ou nom du client", 18);
    private final DefaultComboBoxModel<CompteBancaire> modeleComptes = new DefaultComboBoxModel<>();
    private final JComboBox<CompteBancaire> comboCompte = new JComboBox<>(modeleComptes);
    private final DefaultComboBoxModel<CompteBancaire> modeleDestinations = new DefaultComboBoxModel<>();
    private final JComboBox<CompteBancaire> comboDestination = new JComboBox<>(modeleDestinations);
    private final ChampTexte champMontant = new ChampTexte("Montant en " + Formatage.DEVISE, 14);

    private final JRadioButton rbDepot = new JRadioButton("Dépôt", true);
    private final JRadioButton rbRetrait = new JRadioButton("Retrait");
    private final JRadioButton rbVirement = new JRadioButton("Virement");
    private final JRadioButton rbVip = new JRadioButton("Retrait guichet VIP");

    private final Carte carteInfos = new Carte(null);
    private final JLabel lblTitulaire = valeur();
    private final JLabel lblCategorie = valeur();
    private final JLabel lblSolde = valeur();
    private final JLabel lblDisponible = valeur();
    private final JLabel lblGuichetVip = valeur();
    private final JLabel lblMessageVip = new JLabel(" ");
    private JPanel cellDestination;
    private JPanel ligneVip;

    private final ModeleTable<OperationBancaire> modeleHistorique;
    private final TableModerne tableHistorique;
    private final BoutonModerne boutonValider = new BoutonModerne("Valider l'opération", Theme.SUCCES);
    private boolean miseAJour;

    public PanneauOperations(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Guichet - Opérations",
                "Dépôts, retraits, virements et retrait direct au guichet pour les clients VIP");

        // ---------------------------------------------------------- formulaire
        Carte carteForm = new Carte("Nouvelle opération");
        JPanel corpsForm = new JPanel(new BorderLayout(0, 14));
        corpsForm.setOpaque(false);

        Formulaire f = new Formulaire(230);
        f.ajouter("Rechercher un compte", rechercheCompte);
        f.ajouter("Compte *", comboCompte);
        corpsForm.add(f, BorderLayout.NORTH);

        construireCarteInfos();
        corpsForm.add(carteInfos, BorderLayout.CENTER);

        JPanel bas = new JPanel(new BorderLayout(0, 12));
        bas.setOpaque(false);
        JPanel types = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 2));
        types.setOpaque(false);
        ButtonGroup groupe = new ButtonGroup();
        for (JRadioButton rb : new JRadioButton[]{rbDepot, rbRetrait, rbVirement, rbVip}) {
            rb.setOpaque(false);
            rb.setFont(Theme.police(Font.BOLD, 13));
            groupe.add(rb);
            types.add(rb);
            rb.addActionListener(e -> majTypeOperation());
        }
        rbVip.setForeground(Theme.OR_FONCE);
        bas.add(types, BorderLayout.NORTH);

        Formulaire f2 = new Formulaire(230);
        f2.ajouter("Montant (" + Formatage.DEVISE + ") *", champMontant);
        cellDestination = f2.ajouter("Compte destinataire *", comboDestination);
        cellDestination.setVisible(false);
        bas.add(f2, BorderLayout.CENTER);

        JPanel boutons = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 4));
        boutons.setOpaque(false);
        boutonValider.addActionListener(e -> valider());
        boutons.add(boutonValider);
        bas.add(boutons, BorderLayout.SOUTH);
        corpsForm.add(bas, BorderLayout.SOUTH);
        carteForm.add(corpsForm, BorderLayout.CENTER);

        // ---------------------------------------------------------- historique
        modeleHistorique = new ModeleTable<OperationBancaire>(
                new String[]{"N°", "Date", "Type", "Libellé", "Montant", "Solde après", "Statut"},
                new Class<?>[]{Long.class, String.class, String.class, String.class, Double.class, Double.class, String.class},
                op -> new Object[]{op.getIdOperation(), Formatage.dateHeure(op.getDateOperation()),
                        op.getTypeOperation(), op.getLibelle(), op.getVariation(), op.getSoldeApres(),
                        op.isAnnulee() ? "Annulée" : "Effectuée"});
        tableHistorique = new TableModerne(modeleHistorique);
        tableHistorique.setLargeurColonne(0, 50);
        tableHistorique.setColonnesSecondaires(640, 0, 1, 3, 6);
        JScrollPane spHist = tableHistorique.enDefilement();
        spHist.setPreferredSize(new Dimension(200, 360));

        Carte carteHist = new Carte("Historique du compte");
        carteHist.add(spHist, BorderLayout.CENTER);
        JPanel actionsHist = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 2));
        actionsHist.setOpaque(false);
        BoutonModerne annuler = BoutonModerne.secondaire("Annuler l'opération", Theme.DANGER);
        BoutonModerne releve = BoutonModerne.secondaire("Relevé complet", Theme.INFO);
        annuler.addActionListener(e -> annulerOperation());
        releve.addActionListener(e -> {
            CompteBancaire c = compteSelectionne();
            if (c != null) {
                Messages.texteLong(this, "Relevé " + c.getNumeroCompte(), c.obtenirReleve());
            }
        });
        actionsHist.add(annuler);
        actionsHist.add(releve);
        carteHist.add(actionsHist, BorderLayout.SOUTH);

        // ---------------------------------------------------------- disposition responsive
        PanneauDefilant corps = new PanneauDefilant(new GrilleResponsive(400, 16, 16).setEtirerDerniereLigne(true));
        corps.add(carteForm);
        corps.add(carteHist);
        JScrollPane defilement = new JScrollPane(corps);
        defilement.setBorder(null);
        defilement.setOpaque(false);
        defilement.getViewport().setOpaque(false);
        defilement.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        defilement.getVerticalScrollBar().setUnitIncrement(18);
        add(defilement, BorderLayout.CENTER);

        comboCompte.addActionListener(e -> {
            if (!miseAJour) {
                majInfosCompte();
            }
        });
        rechercheCompte.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                chargerComptes(compteSelectionne());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                chargerComptes(compteSelectionne());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                chargerComptes(compteSelectionne());
            }
        });
    }

    private static JLabel valeur() {
        JLabel l = new JLabel("-");
        l.setFont(Theme.police(Font.BOLD, 14));
        l.setForeground(Theme.TEXTE);
        return l;
    }

    private void construireCarteInfos() {
        JPanel grille = new JPanel(new GridLayout(0, 1, 0, 6));
        grille.setOpaque(false);
        grille.add(ligneInfo("Titulaire", lblTitulaire));
        grille.add(ligneInfo("Catégorie", lblCategorie));
        grille.add(ligneInfo("Solde actuel", lblSolde));
        grille.add(ligneInfo("Disponible (retrait standard)", lblDisponible));
        ligneVip = ligneInfo("Disponible au guichet VIP", lblGuichetVip);
        grille.add(ligneVip);
        carteInfos.add(grille, BorderLayout.CENTER);
        lblMessageVip.setFont(Theme.police(Font.ITALIC, 12));
        lblMessageVip.setForeground(Theme.OR_FONCE);
        carteInfos.add(lblMessageVip, BorderLayout.SOUTH);
    }

    private JPanel ligneInfo(String libelle, JLabel valeur) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(libelle);
        l.setForeground(Theme.TEXTE_SECONDAIRE);
        l.setFont(Theme.police(Font.PLAIN, 13));
        valeur.setHorizontalAlignment(JLabel.RIGHT);
        p.add(l, BorderLayout.WEST);
        p.add(valeur, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------ donnees

    @Override
    public void rafraichir() {
        chargerComptes(compteSelectionne());
    }

    /** Recharge la liste des comptes actifs en conservant la selection. */
    private void chargerComptes(CompteBancaire aSelectionner) {
        miseAJour = true;
        try {
            String filtre = rechercheCompte.getText().trim().toLowerCase();
            List<CompteBancaire> comptes = banque().getComptesActifs();
            modeleComptes.removeAllElements();
            modeleDestinations.removeAllElements();
            for (CompteBancaire c : comptes) {
                modeleDestinations.addElement(c);
                String texte = (c.getNumeroCompte() + " " + c.getTitulaire().getNomComplet() + " "
                        + c.getTitulaire().getIdClient()).toLowerCase();
                if (filtre.isEmpty() || texte.contains(filtre)) {
                    modeleComptes.addElement(c);
                }
            }
            if (aSelectionner != null && modeleComptes.getIndexOf(aSelectionner) >= 0) {
                comboCompte.setSelectedItem(aSelectionner);
            } else if (modeleComptes.getSize() > 0) {
                comboCompte.setSelectedIndex(0);
            }
        } finally {
            miseAJour = false;
        }
        majInfosCompte();
    }

    public void selectionnerCompte(CompteBancaire c) {
        rechercheCompte.setText("");
        chargerComptes(c);
    }

    private CompteBancaire compteSelectionne() {
        return (CompteBancaire) comboCompte.getSelectedItem();
    }

    private void majInfosCompte() {
        CompteBancaire c = compteSelectionne();
        boolean vip = c != null && c.getTitulaire() instanceof ClientVIP;
        if (c == null) {
            lblTitulaire.setText("-");
            lblCategorie.setText("-");
            lblSolde.setText("-");
            lblDisponible.setText("-");
            lblMessageVip.setText("Aucun compte actif.");
            modeleHistorique.setDonnees(new ArrayList<OperationBancaire>());
        } else {
            lblTitulaire.setText(c.getTitulaire().getNomComplet());
            lblCategorie.setText(c.getTitulaire().getCategorie() + " - " + c.getTypeCompte());
            lblCategorie.setForeground(vip ? Theme.OR_FONCE : Theme.TEXTE);
            lblSolde.setText(Formatage.monnaie(c.consulterSolde()));
            lblSolde.setForeground(c.consulterSolde() < 0 ? Theme.DANGER : Theme.SUCCES);
            lblDisponible.setText(Formatage.monnaie(c.getMontantDisponible()));
            if (vip) {
                double guichet = Math.max(0, c.consulterSolde() + c.limiteDebitVIP());
                lblGuichetVip.setText(Formatage.monnaie(guichet));
                lblMessageVip.setText("Client VIP : retrait direct au guichet autorisé même si le solde est "
                        + "insuffisant (limite -" + Formatage.monnaie(c.limiteDebitVIP()) + ").");
            } else {
                lblMessageVip.setText(" ");
            }
            List<OperationBancaire> ops = new ArrayList<>(c.getOperations());
            java.util.Collections.reverse(ops);
            modeleHistorique.setDonnees(ops);
        }
        ligneVip.setVisible(vip);
        carteInfos.setFond(vip ? Theme.OR_CLAIR : Color.WHITE);
        carteInfos.setCouleurBordure(vip ? Theme.OR : Theme.BORDURE);
        rbVip.setEnabled(vip);
        if (!vip && rbVip.isSelected()) {
            rbRetrait.setSelected(true);
        }
        boutonValider.setEnabled(c != null);
        majTypeOperation();
    }

    private void majTypeOperation() {
        cellDestination.setVisible(rbVirement.isSelected());
        if (rbVip.isSelected()) {
            boutonValider.setText("Retrait au guichet VIP");
            boutonValider.setFond(Theme.OR_FONCE);
        } else if (rbRetrait.isSelected()) {
            boutonValider.setText("Valider le retrait");
            boutonValider.setFond(Theme.DANGER);
        } else if (rbVirement.isSelected()) {
            boutonValider.setText("Valider le virement");
            boutonValider.setFond(Theme.PRIMAIRE_CLAIR);
        } else {
            boutonValider.setText("Valider le dépôt");
            boutonValider.setFond(Theme.SUCCES);
        }
        boutonValider.revalidate();
        cellDestination.getParent().revalidate();
    }

    // ------------------------------------------------------------ actions

    private void valider() {
        CompteBancaire c = compteSelectionne();
        if (c == null) {
            Messages.erreur(this, "Veuillez sélectionner un compte.");
            return;
        }
        double montant;
        try {
            montant = Formatage.lireMontant(champMontant.getText());
        } catch (NumberFormatException ex) {
            Messages.erreur(this, "Montant invalide. Exemple : 150000 ou 150 000,50");
            champMontant.requestFocusInWindow();
            return;
        }
        try {
            if (rbDepot.isSelected()) {
                service.deposer(c, montant);
                succes("Dépôt de " + Formatage.monnaie(montant) + " effectué.", c);
            } else if (rbRetrait.isSelected()) {
                retraitStandard(c, montant);
            } else if (rbVirement.isSelected()) {
                CompteBancaire dest = (CompteBancaire) comboDestination.getSelectedItem();
                if (dest == null) {
                    throw new BanqueException("Veuillez choisir le compte destinataire.");
                }
                if (!Messages.confirmer(this, "Confirmer le virement", "Virer " + Formatage.monnaie(montant)
                        + "\nde " + c.getNumeroCompte() + " (" + c.getTitulaire().getNomComplet() + ")"
                        + "\nvers " + dest.getNumeroCompte() + " (" + dest.getTitulaire().getNomComplet() + ") ?")) {
                    return;
                }
                service.virer(c, dest, montant);
                succes("Virement de " + Formatage.monnaie(montant) + " vers " + dest.getNumeroCompte() + " effectué.", c);
            } else if (rbVip.isSelected()) {
                retraitVip(c, montant);
            }
        } catch (BanqueException ex) {
            Messages.erreur(this, ex.getMessage());
        }
    }

    private void retraitStandard(CompteBancaire c, double montant) throws BanqueException {
        try {
            service.retirer(c, montant);
            succes("Retrait de " + Formatage.monnaie(montant) + " effectué.", c);
        } catch (SoldeInsuffisantException ex) {
            if (!ex.isClientVIP()) {
                throw ex;
            }
            // Le client est VIP : on propose le retrait direct au guichet
            if (Messages.confirmer(this, "Solde insuffisant - Client VIP",
                    ex.getMessage() + "\n\nEffectuer le retrait directement au guichet de la banque ?")) {
                service.retirerAuGuichetVIP(c, montant);
                succesVip(c, montant);
            }
        }
    }

    private void retraitVip(CompteBancaire c, double montant) throws BanqueException {
        double soldeApres = c.consulterSolde() - montant;
        if (soldeApres < 0) {
            if (!Messages.confirmerAttention(this, "Retrait guichet VIP",
                    "Le solde du compte deviendra " + Formatage.monnaie(soldeApres)
                            + ".\nLe client VIP " + c.getTitulaire().getNomComplet()
                            + " bénéficie du privilège de retrait au guichet.\n\nConfirmer le retrait de "
                            + Formatage.monnaie(montant) + " ?")) {
                return;
            }
        }
        service.retirerAuGuichetVIP(c, montant);
        succesVip(c, montant);
    }

    private void succes(String message, CompteBancaire c) {
        champMontant.setText("");
        Messages.succes(this, message + "\nNouveau solde : " + Formatage.monnaie(c.consulterSolde()));
    }

    private void succesVip(CompteBancaire c, double montant) {
        champMontant.setText("");
        Messages.succes(this, "Retrait au guichet VIP de " + Formatage.monnaie(montant) + " effectué.\n"
                + "Nouveau solde : " + Formatage.monnaie(c.consulterSolde())
                + "\nEncore disponible au guichet : "
                + Formatage.monnaie(Math.max(0, c.consulterSolde() + c.limiteDebitVIP())));
    }

    private void annulerOperation() {
        OperationBancaire op = modeleHistorique.getObjet(tableHistorique.ligneModeleSelectionnee());
        if (op == null) {
            Messages.info(this, "Sélection", "Sélectionnez une opération dans l'historique.");
            return;
        }
        if (!Messages.confirmerAttention(this, "Annuler l'opération",
                "Annuler l'opération #" + op.getIdOperation() + " (" + op.getTypeOperation() + " de "
                        + Formatage.monnaie(op.getMontant()) + ") ?\nUne opération inverse sera enregistrée.")) {
            return;
        }
        try {
            service.annulerOperation(op);
            Messages.succes(this, "Opération #" + op.getIdOperation() + " annulée.");
        } catch (BanqueException ex) {
            Messages.erreur(this, ex.getMessage());
        }
    }
}
