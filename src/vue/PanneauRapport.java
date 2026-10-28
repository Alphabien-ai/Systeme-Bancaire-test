package vue;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import service.BanqueService;
import vue.composants.BoutonModerne;
import vue.composants.Carte;

/**
 * Rapport general de la banque, traitement mensuel et export.
 */
public class PanneauRapport extends PanneauBase {
    private static final long serialVersionUID = 1L;

    private final JTextArea zone = new JTextArea();

    public PanneauRapport(BanqueService service, FenetrePrincipale fenetre) {
        super(service, fenetre, "Rapports", "Rapport général, traitement mensuel et export");

        zone.setEditable(false);
        zone.setFont(Theme.policeMono(13));
        zone.setForeground(Theme.TEXTE);
        zone.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        BoutonModerne actualiser = new BoutonModerne("Actualiser", Theme.PRIMAIRE_CLAIR);
        BoutonModerne mensuel = BoutonModerne.secondaire("Traitement mensuel (frais & intérêts)", Theme.AVERTISSEMENT);
        BoutonModerne exporter = BoutonModerne.secondaire("Exporter (.txt)", Theme.SUCCES);
        BoutonModerne reinit = BoutonModerne.secondaire("Réinitialiser les données", Theme.DANGER);
        actualiser.addActionListener(e -> rafraichir());
        mensuel.addActionListener(e -> traitementMensuel());
        exporter.addActionListener(e -> exporter());
        reinit.addActionListener(e -> reinitialiser());
        ajouterAction(actualiser);
        ajouterAction(mensuel);
        ajouterAction(exporter);
        ajouterAction(reinit);

        JScrollPane sp = new JScrollPane(zone);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        add(new Carte(null, sp), BorderLayout.CENTER);
    }

    @Override
    public void rafraichir() {
        zone.setText(banque().consulterRapport() + "\n  Fichier de données : " + service.getCheminDonnees() + "\n");
        zone.setCaretPosition(0);
    }

    private void traitementMensuel() {
        if (!Messages.confirmer(this, "Traitement mensuel",
                "Prélever les frais de gestion et agios des comptes courants\n"
                        + "et verser les intérêts des comptes épargne ?")) {
            return;
        }
        String resultat = service.traitementMensuel();
        Messages.succes(this, resultat);
    }

    private void exporter() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("rapport_banque.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = fc.getSelectedFile();
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            w.write(zone.getText());
            Messages.succes(this, "Rapport exporté :\n" + f.getAbsolutePath());
        } catch (IOException ex) {
            Messages.erreur(this, "Export impossible : " + ex.getMessage());
        }
    }

    private void reinitialiser() {
        if (Messages.confirmerAttention(this, "Réinitialiser",
                "Toutes les données actuelles seront remplacées par les données de démonstration.\nContinuer ?")) {
            service.reinitialiserDemo();
            for (PanneauBase p : fenetre.getEcrans()) {
                p.rafraichir();
            }
            Messages.succes(this, "Données de démonstration rechargées.");
        }
    }
}
