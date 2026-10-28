package service;

import modele.Agence;
import modele.Banque;
import modele.CarteBancaire;
import modele.Client;
import modele.ClientVIP;
import modele.CompteBancaire;
import modele.CompteCourant;
import modele.CompteEpargne;
import modele.ServiceBancaire;

/**
 * Jeu de donnees de demonstration cree au premier lancement.
 */
public final class DonneesDemo {

    private DonneesDemo() {
    }

    public static Banque creer() {
        Banque banque = new Banque("BNK-MG-001", "Banque ASJA Madagascar", "Avenue de l'Indépendance, Antananarivo", 50_000_000_000.0);

        Agence tana = new Agence(banque.genererCodeAgence(), "Agence Analakely", "Analakely, Antananarivo", "020 22 000 01");
        Agence tamatave = new Agence(banque.genererCodeAgence(), "Agence Toamasina", "Bd Joffre, Toamasina", "020 53 000 02");
        Agence fianar = new Agence(banque.genererCodeAgence(), "Agence Fianarantsoa", "Centre-ville, Fianarantsoa", "020 75 000 03");
        banque.ajouterAgence(tana);
        banque.ajouterAgence(tamatave);
        banque.ajouterAgence(fianar);

        ServiceBancaire sms = new ServiceBancaire(banque.genererCodeService(), "SMS Banking", "Alertes SMS à chaque opération", 2_000);
        ServiceBancaire web = new ServiceBancaire(banque.genererCodeService(), "E-Banking", "Accès au compte en ligne 24h/24", 5_000);
        ServiceBancaire assur = new ServiceBancaire(banque.genererCodeService(), "Assurance Moyens de paiement", "Protection contre la fraude et le vol de carte", 7_500);
        ServiceBancaire conseil = new ServiceBancaire(banque.genererCodeService(), "Conseiller Prive", "Gestionnaire de patrimoine dédié (VIP)", 25_000);
        banque.ajouterService(sms);
        banque.ajouterService(web);
        banque.ajouterService(assur);
        banque.ajouterService(conseil);

        Client c1 = new Client(banque.genererIdClient(), "Rakoto", "Jean", "Lot II A 45, Antananarivo", "034 11 222 33");
        Client c2 = new Client(banque.genererIdClient(), "Rasoa", "Marie", "Ampasimazava, Toamasina", "032 44 555 66");
        Client c3 = new Client(banque.genererIdClient(), "Randria", "Paul", "Tanambao, Fianarantsoa", "033 77 888 99");
        ClientVIP v1 = new ClientVIP(banque.genererIdClient(), "Andriamanana", "Hery", "Ivandry, Antananarivo",
                "034 00 111 22", 10_000_000, "Mme Rabe");
        ClientVIP v2 = new ClientVIP(banque.genererIdClient(), "Ramanantsoa", "Fara", "Ankorondrano, Antananarivo",
                "032 99 000 11", 5_000_000, "M. Rajaona");

        tana.gererClient(c1);
        tamatave.gererClient(c2);
        fianar.gererClient(c3);
        tana.gererClient(v1);
        tana.gererClient(v2);

        CompteBancaire cc1 = ouvrirCourant(banque, tana, c1, 200_000, 3_000, 850_000);
        ouvrirEpargne(banque, tana, c1, 4.5, 50_000, 1_500_000);
        ouvrirCourant(banque, tamatave, c2, 100_000, 3_000, 420_000);
        ouvrirEpargne(banque, fianar, c3, 3.5, 50_000, 300_000);
        CompteBancaire ccv1 = ouvrirCourant(banque, tana, v1, 2_000_000, 0, 12_500_000);
        ouvrirEpargne(banque, tana, v1, 6.0, 100_000, 40_000_000);
        CompteBancaire ccv2 = ouvrirCourant(banque, tana, v2, 1_000_000, 0, 250_000);

        cc1.retirer(120_000);
        ccv1.retirer(2_500_000);
        // Exemple : compte VIP deja en negatif grace au retrait guichet
        ccv2.retirerAuGuichetVIP(1_800_000);

        CarteBancaire k1 = c1.demanderCarte(banque.genererNumeroCarte(), cc1, CarteBancaire.TYPES[0]);
        k1.activer();
        CarteBancaire k2 = v1.demanderCarte(banque.genererNumeroCarte(), ccv1, CarteBancaire.TYPES[3]);
        k2.activer();

        sms.souscrire(c1);
        web.souscrire(c1);
        web.souscrire(v1);
        conseil.souscrire(v1);
        assur.souscrire(v2);
        return banque;
    }

    private static CompteBancaire ouvrirCourant(Banque b, Agence a, Client c, double decouvert, double frais, double depot) {
        CompteCourant cc = new CompteCourant(b.genererNumeroCompte(true), c, decouvert, frais);
        a.ouvrirCompte(cc);
        cc.deposer(depot);
        return cc;
    }

    private static CompteBancaire ouvrirEpargne(Banque b, Agence a, Client c, double taux, double min, double depot) {
        CompteEpargne ce = new CompteEpargne(b.genererNumeroCompte(false), c, taux, min);
        a.ouvrirCompte(ce);
        ce.deposer(depot);
        return ce;
    }
}
