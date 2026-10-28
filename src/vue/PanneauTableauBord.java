package vue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JScrollPane;

import modele.Banque;
import modele.ClientVIP;
import modele.CompteBancaire;
import modele.OperationBancaire;
import service.BanqueService;
import util.Formatage;
import vue.composants.BoutonModerne;
import vue.composants.Carte;
import vue.composants.CarteStatistique;
import vue.composants.GrilleResponsive;
import vue.composants.ModeleTable;
import vue.composants.PanneauDefilant;
import vue.composants.TableModerne;

/**
 * Tableau de bord : indicateurs cles et activite recente.
 */
public class PanneauTableauBord extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final CarteStatistique kpiClients = new CarteStatistique("Clients", "CL", Theme.VIOLET);
    private final CarteStatistique kpiComptes = new CarteStatistique("Comptes actifs", "CP", Theme.INFO);
    private final CarteStatistique kpiDepots = new CarteStatistique("Total des dépôts", "Ar", Theme.SUCCES);
    private final CarteStatistique kpiDebit = new CarteStatistique("Encours débiteur", "-", Theme.DANGER);
    private final CarteStatistique kpiVip = new CarteStatistique("Clients VIP", "VIP", Theme.OR);
    private final CarteStatistique kpiOperations = new CarteStatistique("Opérations", "OP", Theme.PRIMAIRE_CLAIR);

    private final ModeleTable<OperationBancaire> modeleOps;
    private final ModeleTable<CompteBancaire> modeleVip;

    public PanneauTableauBord(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Tableau de bord", "Vue d'ensemble de l'activité de la banque");

        BoutonModerne guichet = new BoutonModerne("Nouvelle opération", Theme.SUCCES);
        guichet.addActionListener(e -> fenetre.afficher(FenetrePrincipale.OPERATIONS));
        BoutonModerne nouveauClient = BoutonModerne.secondaire("Gérer les clients", Theme.PRIMAIRE_CLAIR);
        nouveauClient.addActionListener(e -> fenetre.afficher(FenetrePrincipale.CLIENTS));
        BoutonModerne actualiser = BoutonModerne.secondaire("Actualiser", Theme.TEXTE_SECONDAIRE);
        actualiser.addActionListener(e -> rafraichir());
        ajouterAction(guichet);
        ajouterAction(nouveauClient);
        ajouterAction(actualiser);

        PanneauDefilant corps = new PanneauDefilant(new BorderLayout(0, 16));

        javax.swing.JPanel grilleKpi = new javax.swing.JPanel(new GrilleResponsive(250, 14, 14));
        grilleKpi.setOpaque(false);
        grilleKpi.add(kpiClients);
        grilleKpi.add(kpiComptes);
        grilleKpi.add(kpiDepots);
        grilleKpi.add(kpiDebit);
        grilleKpi.add(kpiVip);
        grilleKpi.add(kpiOperations);
        corps.add(grilleKpi, BorderLayout.NORTH);

        modeleOps = new ModeleTable<OperationBancaire>(
                new String[]{"N°", "Date", "Compte", "Type", "Montant"},
                new Class<?>[]{Long.class, String.class, String.class, String.class, Double.class},
                op -> new Object[]{op.getIdOperation(), Formatage.dateHeure(op.getDateOperation()),
                        op.getCompte().getNumeroCompte(), op.getTypeOperation() + (op.isAnnulee() ? " (annulée)" : ""),
                        op.getVariation()});
        TableModerne tableOps = new TableModerne(modeleOps);
        tableOps.setLargeurColonne(0, 50);
        tableOps.setColonnesSecondaires(520, 0, 1);
        JScrollPane spOps = tableOps.enDefilement();
        spOps.setPreferredSize(new Dimension(200, 300));

        modeleVip = new ModeleTable<CompteBancaire>(
                new String[]{"Client VIP", "Compte", "Solde", "Disponible au guichet"},
                new Class<?>[]{String.class, String.class, Double.class, Double.class},
                c -> new Object[]{c.getTitulaire().getNomComplet(), c.getNumeroCompte(), c.consulterSolde(),
                        Math.max(0, c.consulterSolde() + c.limiteDebitVIP())});
        TableModerne tableVip = new TableModerne(modeleVip);
        tableVip.setColonnesSecondaires(520, 1);
        JScrollPane spVip = tableVip.enDefilement();
        spVip.setPreferredSize(new Dimension(200, 300));

        javax.swing.JPanel grilleTables = new javax.swing.JPanel(
                new GrilleResponsive(440, 16, 16).setEtirerDerniereLigne(true));
        grilleTables.setOpaque(false);
        grilleTables.add(new Carte("Dernières opérations", spOps));
        Carte carteVip = new Carte("Comptes VIP - retrait au guichet", spVip);
        carteVip.setCouleurBordure(Theme.OR);
        grilleTables.add(carteVip);
        corps.add(grilleTables, BorderLayout.CENTER);

        JScrollPane defilement = new JScrollPane(corps);
        defilement.setBorder(null);
        defilement.setOpaque(false);
        defilement.getViewport().setOpaque(false);
        defilement.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        defilement.getVerticalScrollBar().setUnitIncrement(18);
        add(defilement, BorderLayout.CENTER);
    }

    @Override
    public void rafraichir() {
        Banque b = banque();
        List<CompteBancaire> actifs = b.getComptesActifs();
        int courants = 0;
        int debiteurs = 0;
        for (CompteBancaire c : actifs) {
            if (c instanceof modele.CompteCourant) {
                courants++;
            }
            if (c.consulterSolde() < 0) {
                debiteurs++;
            }
        }
        kpiClients.setValeur(String.valueOf(b.getTousLesClients().size()), b.getAgences().size() + " agence(s)");
        kpiComptes.setValeur(String.valueOf(actifs.size()),
                courants + " courant(s), " + (actifs.size() - courants) + " épargne(s)");
        kpiDepots.setValeur(Formatage.monnaie(b.getTotalDepots()), "Capital : " + Formatage.monnaie(b.getCapital()));
        kpiDebit.setValeur(Formatage.monnaie(b.getEncoursNegatif()), debiteurs + " compte(s) débiteur(s)");

        double plafondTotal = 0;
        List<CompteBancaire> comptesVip = new ArrayList<>();
        for (modele.Client c : b.getTousLesClients()) {
            if (c instanceof ClientVIP) {
                plafondTotal += ((ClientVIP) c).getPlafondCreditVIP();
                for (CompteBancaire cpt : c.getComptes()) {
                    if (cpt.isActif()) {
                        comptesVip.add(cpt);
                    }
                }
            }
        }
        kpiVip.setValeur(String.valueOf(b.getNombreVIP()), "Plafonds : " + Formatage.monnaie(plafondTotal));

        List<OperationBancaire> ops = b.getToutesLesOperations();
        Calendar debutJour = Calendar.getInstance();
        debutJour.set(Calendar.HOUR_OF_DAY, 0);
        debutJour.set(Calendar.MINUTE, 0);
        debutJour.set(Calendar.SECOND, 0);
        int aujourdhui = 0;
        for (OperationBancaire op : ops) {
            if (op.getDateOperation().after(debutJour.getTime())) {
                aujourdhui++;
            }
        }
        kpiOperations.setValeur(String.valueOf(ops.size()), aujourdhui + " aujourd'hui");

        modeleOps.setDonnees(ops.subList(0, Math.min(15, ops.size())));
        modeleVip.setDonnees(comptesVip);
    }
}
