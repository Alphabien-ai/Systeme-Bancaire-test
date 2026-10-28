package vue.composants;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Modele de table generique : chaque ligne correspond a un objet metier.
 * @param <T> type de l'objet affiche
 */
public class ModeleTable<T> extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    /** Transforme un objet en tableau de valeurs de colonnes. */
    public interface Convertisseur<T> {
        Object[] convertir(T objet);
    }

    private final String[] colonnes;
    private final Class<?>[] types;
    private final Convertisseur<T> convertisseur;
    private final List<T> objets = new ArrayList<>();
    private final List<Object[]> lignes = new ArrayList<>();

    public ModeleTable(String[] colonnes, Class<?>[] types, Convertisseur<T> convertisseur) {
        this.colonnes = colonnes;
        this.types = types;
        this.convertisseur = convertisseur;
    }

    public void setDonnees(List<T> donnees) {
        objets.clear();
        lignes.clear();
        for (T o : donnees) {
            objets.add(o);
            lignes.add(convertisseur.convertir(o));
        }
        fireTableDataChanged();
    }

    public T getObjet(int ligneModele) {
        if (ligneModele < 0 || ligneModele >= objets.size()) {
            return null;
        }
        return objets.get(ligneModele);
    }

    public int indexDe(T objet) {
        return objets.indexOf(objet);
    }

    @Override
    public int getRowCount() {
        return lignes.size();
    }

    @Override
    public int getColumnCount() {
        return colonnes.length;
    }

    @Override
    public String getColumnName(int column) {
        return colonnes[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (types != null && columnIndex < types.length && types[columnIndex] != null) {
            return types[columnIndex];
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object[] l = lignes.get(rowIndex);
        return columnIndex < l.length ? l[columnIndex] : null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
