package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import modele.Banque;

/**
 * Sauvegarde / chargement des donnees par serialisation dans un fichier.
 */
public class Persistance {

    private final File fichier;

    public Persistance(String chemin) {
        this.fichier = new File(chemin);
    }

    public boolean existe() {
        return fichier.exists() && fichier.length() > 0;
    }

    public void sauvegarder(Banque banque) throws IOException {
        banque.memoriserCompteurs();
        File temp = new File(fichier.getAbsolutePath() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(temp))) {
            out.writeObject(banque);
        }
        if (fichier.exists() && !fichier.delete()) {
            throw new IOException("Impossible de remplacer " + fichier.getName());
        }
        if (!temp.renameTo(fichier)) {
            throw new IOException("Impossible d'écrire " + fichier.getName());
        }
    }

    public Banque charger() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fichier))) {
            Banque banque = (Banque) in.readObject();
            banque.restaurerCompteurs();
            return banque;
        }
    }

    public String getChemin() {
        return fichier.getAbsolutePath();
    }
}
