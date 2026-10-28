package modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.Formatage;

/**
 * Service bancaire propose par la banque (assurance, SMS banking, etc.).
 */
public class ServiceBancaire implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codeService;
    private String nomService;
    private String description;
    private double tarifMensuel;
    private final List<Client> souscripteurs = new ArrayList<>();

    public ServiceBancaire(String codeService, String nomService, String description, double tarifMensuel) {
        this.codeService = codeService;
        this.nomService = nomService;
        this.description = description;
        this.tarifMensuel = tarifMensuel;
    }

    public void souscrire(Client client) {
        if (souscripteurs.contains(client)) {
            throw new IllegalStateException(client.getNomComplet() + " a déjà souscrit à ce service.");
        }
        souscripteurs.add(client);
        client.ajouterService(this);
    }

    public void resilier(Client client) {
        if (!souscripteurs.remove(client)) {
            throw new IllegalStateException(client.getNomComplet() + " n'a pas souscrit à ce service.");
        }
        client.retirerService(this);
    }

    public String consulter() {
        StringBuilder sb = new StringBuilder();
        sb.append("Service    : ").append(nomService).append(" (").append(codeService).append(")\n");
        sb.append("Tarif      : ").append(Formatage.monnaie(tarifMensuel)).append(" / mois\n");
        sb.append("Description: ").append(description).append('\n');
        sb.append("Souscripteurs (").append(souscripteurs.size()).append(") :\n");
        for (Client c : souscripteurs) {
            sb.append("   - ").append(c).append('\n');
        }
        return sb.toString();
    }

    public String getCodeService() { return codeService; }
    public String getNomService() { return nomService; }
    public void setNomService(String n) { this.nomService = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public double getTarifMensuel() { return tarifMensuel; }
    public void setTarifMensuel(double t) { this.tarifMensuel = t; }
    public List<Client> getSouscripteurs() { return Collections.unmodifiableList(souscripteurs); }

    @Override
    public String toString() {
        return nomService;
    }
}
