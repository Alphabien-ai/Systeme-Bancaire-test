package modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client standard de la banque.
 */
public class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String idClient;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private Agence agence;
    private final List<CompteBancaire> comptes = new ArrayList<>();
    private final List<CarteBancaire> cartes = new ArrayList<>();
    private final List<ServiceBancaire> services = new ArrayList<>();

    public Client(String idClient, String nom, String prenom, String adresse, String telephone) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    /** Associe un compte deja cree au client. */
    public void ouvrirCompte(CompteBancaire compte) {
        if (compte != null && !comptes.contains(compte)) {
            comptes.add(compte);
        }
    }

    void retirerCompte(CompteBancaire compte) {
        comptes.remove(compte);
    }

    /** Demande une carte bancaire liee a un compte du client. */
    public CarteBancaire demanderCarte(String numeroCarte, CompteBancaire compte, String typeCarte) {
        if (!comptes.contains(compte)) {
            throw new IllegalArgumentException("Ce compte n'appartient pas au client.");
        }
        CarteBancaire carte = new CarteBancaire(numeroCarte, typeCarte, compte);
        cartes.add(carte);
        compte.ajouterCarte(carte);
        return carte;
    }

    /** Solde total de tous les comptes actifs. */
    public double consulterSolde() {
        double total = 0;
        for (CompteBancaire c : comptes) {
            if (c.isActif()) {
                total += c.consulterSolde();
            }
        }
        return total;
    }

    public boolean estVIP() {
        return false;
    }

    public String getCategorie() {
        return "Standard";
    }

    void ajouterService(ServiceBancaire s) {
        if (!services.contains(s)) {
            services.add(s);
        }
    }

    void retirerService(ServiceBancaire s) {
        services.remove(s);
    }

    public String getNomComplet() {
        return prenom + " " + nom.toUpperCase();
    }

    public String getIdClient() { return idClient; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public Agence getAgence() { return agence; }
    void setAgence(Agence agence) { this.agence = agence; }
    public List<CompteBancaire> getComptes() { return Collections.unmodifiableList(comptes); }
    public List<CarteBancaire> getCartes() { return Collections.unmodifiableList(cartes); }
    public List<ServiceBancaire> getServices() { return Collections.unmodifiableList(services); }

    @Override
    public String toString() {
        return idClient + " - " + getNomComplet() + (estVIP() ? " [VIP]" : "");
    }
}
