package modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agence bancaire : gere ses clients et ses comptes.
 */
public class Agence implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codeAgence;
    private String nomAgence;
    private String adresse;
    private String telephone;
    private Banque banque;
    private final List<Client> clients = new ArrayList<>();
    private final List<CompteBancaire> comptes = new ArrayList<>();

    public Agence(String codeAgence, String nomAgence, String adresse, String telephone) {
        this.codeAgence = codeAgence;
        this.nomAgence = nomAgence;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    /** Rattache un client a l'agence. */
    public void gererClient(Client client) {
        if (client.getAgence() != null && client.getAgence() != this) {
            client.getAgence().clients.remove(client);
        }
        if (!clients.contains(client)) {
            clients.add(client);
        }
        client.setAgence(this);
    }

    /** Detache un client de l'agence (ses comptes doivent etre fermes). */
    public void supprimerClient(Client client) {
        clients.remove(client);
        client.setAgence(null);
    }

    /** Ouvre (enregistre) un compte dans l'agence. */
    public void ouvrirCompte(CompteBancaire compte) {
        if (compte.getTitulaire().getAgence() != this) {
            gererClient(compte.getTitulaire());
        }
        comptes.add(compte);
        compte.setAgence(this);
        compte.getTitulaire().ouvrirCompte(compte);
    }

    /**
     * Ferme un compte : son solde doit etre exactement nul.
     */
    public void fermerCompte(CompteBancaire compte) {
        if (Math.abs(compte.consulterSolde()) > 0.001) {
            throw new IllegalStateException("Impossible de fermer le compte : le solde doit être nul (solde actuel "
                    + util.Formatage.monnaie(compte.consulterSolde()) + ").");
        }
        compte.setActif(false);
        for (CarteBancaire carte : compte.getCartes()) {
            carte.bloquer();
        }
    }

    public double getTotalDepots() {
        double total = 0;
        for (CompteBancaire c : comptes) {
            if (c.isActif()) {
                total += c.consulterSolde();
            }
        }
        return total;
    }

    public String getCodeAgence() { return codeAgence; }
    public String getNomAgence() { return nomAgence; }
    public void setNomAgence(String n) { this.nomAgence = n; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String a) { this.adresse = a; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String t) { this.telephone = t; }
    public Banque getBanque() { return banque; }
    void setBanque(Banque b) { this.banque = b; }
    public List<Client> getClients() { return Collections.unmodifiableList(clients); }
    public List<CompteBancaire> getComptes() { return Collections.unmodifiableList(comptes); }

    @Override
    public String toString() {
        return codeAgence + " - " + nomAgence;
    }
}
