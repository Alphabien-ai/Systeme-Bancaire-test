package vue.composants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import util.Formatage;
import vue.Theme;

/**
 * JTable stylisee et RESPONSIVE : lignes alternees, montants formates,
 * couleurs de statut, tri, filtre, et masquage automatique des colonnes
 * secondaires lorsque l'espace horizontal devient trop petit.
 */
public class TableModerne extends JTable {
    private static final long serialVersionUID = 1L;

    private final TableRowSorter<TableModel> trieur;
    private final List<TableColumn> toutesColonnes = new ArrayList<>();
    private final Set<Integer> colonnesSecondaires = new HashSet<>();
    private int seuilCompact = 760;
    private boolean compact = false;
    private RowFilter<TableModel, Integer> filtreSupplementaire;
    private String texteRecherche = "";

    public TableModerne(TableModel modele) {
        super(modele);
        trieur = new TableRowSorter<>(modele);
        setRowSorter(trieur);
        setRowHeight(34);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(Theme.BORDURE);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(Theme.SELECTION);
        setSelectionForeground(Theme.TEXTE);
        setFillsViewportHeight(true);
        setBackground(Color.WHITE);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFont(Theme.police(Font.PLAIN, 13));
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader entete = getTableHeader();
        entete.setReorderingAllowed(false);
        entete.setDefaultRenderer(new RenduEntete());

        RenduCellule rendu = new RenduCellule();
        setDefaultRenderer(Object.class, rendu);
        setDefaultRenderer(String.class, rendu);
        setDefaultRenderer(Double.class, rendu);
        setDefaultRenderer(Integer.class, rendu);
        setDefaultRenderer(Long.class, rendu);

        Enumeration<TableColumn> e = getColumnModel().getColumns();
        while (e.hasMoreElements()) {
            toutesColonnes.add(e.nextElement());
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                adapterColonnes();
            }
        });
    }

    /** Colonnes masquees automatiquement sur les petits ecrans. */
    public void setColonnesSecondaires(int seuil, int... indices) {
        this.seuilCompact = seuil;
        for (int i : indices) {
            colonnesSecondaires.add(i);
        }
        adapterColonnes();
    }

    public void setLargeurColonne(int index, int largeur) {
        if (index < toutesColonnes.size()) {
            toutesColonnes.get(index).setPreferredWidth(largeur);
        }
    }

    private void adapterColonnes() {
        int largeur = getParent() != null ? getParent().getWidth() : getWidth();
        if (largeur <= 0 || colonnesSecondaires.isEmpty()) {
            return;
        }
        boolean nouveauCompact = largeur < seuilCompact;
        if (nouveauCompact == compact) {
            return;
        }
        compact = nouveauCompact;
        while (getColumnModel().getColumnCount() > 0) {
            getColumnModel().removeColumn(getColumnModel().getColumn(0));
        }
        for (int i = 0; i < toutesColonnes.size(); i++) {
            if (!compact || !colonnesSecondaires.contains(i)) {
                getColumnModel().addColumn(toutesColonnes.get(i));
            }
        }
    }

    // ------------------------------------------------------------ filtres

    public void rechercher(String texte) {
        this.texteRecherche = texte == null ? "" : texte.trim();
        appliquerFiltres();
    }

    public void setFiltreSupplementaire(RowFilter<TableModel, Integer> filtre) {
        this.filtreSupplementaire = filtre;
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        List<RowFilter<TableModel, Integer>> filtres = new ArrayList<>();
        if (!texteRecherche.isEmpty()) {
            filtres.add(RowFilter.<TableModel, Integer>regexFilter("(?i)" + Pattern.quote(texteRecherche)));
        }
        if (filtreSupplementaire != null) {
            filtres.add(filtreSupplementaire);
        }
        if (filtres.isEmpty()) {
            trieur.setRowFilter(null);
        } else {
            trieur.setRowFilter(RowFilter.andFilter(filtres));
        }
    }

    /** Index (dans le modele) de la ligne selectionnee, ou -1. */
    public int ligneModeleSelectionnee() {
        int v = getSelectedRow();
        return v < 0 ? -1 : convertRowIndexToModel(v);
    }

    /** Selectionne la ligne correspondant a l'index du modele. */
    public void selectionnerLigneModele(int ligneModele) {
        if (ligneModele < 0 || ligneModele >= getModel().getRowCount()) {
            return;
        }
        int v = convertRowIndexToView(ligneModele);
        if (v >= 0) {
            setRowSelectionInterval(v, v);
            scrollRectToVisible(getCellRect(v, 0, true));
        }
    }

    public JScrollPane enDefilement() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ------------------------------------------------------------ rendus

    private static class RenduEntete extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
            l.setOpaque(true);
            l.setBackground(Theme.ENTETE_TABLE);
            l.setForeground(Theme.PRIMAIRE);
            l.setFont(Theme.police(Font.BOLD, 12));
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.BORDURE),
                    BorderFactory.createEmptyBorder(9, 10, 9, 10)));
            Class<?> type = table.getColumnClass(column);
            l.setHorizontalAlignment(Number.class.isAssignableFrom(type) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return l;
        }
    }

    private static class RenduCellule extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final Font normale = Theme.police(Font.PLAIN, 13);
        private final Font grasse = Theme.police(Font.BOLD, 13);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Object affiche = value;
            if (value instanceof Double) {
                affiche = Formatage.monnaie((Double) value);
            }
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, affiche, isSelected, false, row, column);
            l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            l.setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setFont(normale);
            Color texte = Theme.TEXTE;
            if (value instanceof Double && (Double) value < 0) {
                texte = Theme.DANGER;
                l.setFont(grasse);
            } else if (value instanceof String) {
                String s = (String) value;
                if ("VIP".equals(s) || s.startsWith("RETRAIT GUICHET VIP") || s.contains("(VIP)")) {
                    texte = Theme.OR_FONCE;
                    l.setFont(grasse);
                } else if ("Actif".equals(s) || "Active".equals(s) || s.startsWith("DEPOT")
                        || s.startsWith("VIREMENT RECU") || s.startsWith("INTERETS")) {
                    texte = Theme.SUCCES;
                } else if ("Fermé".equals(s) || "Bloquee".equals(s) || "Annulée".equals(s)
                        || s.startsWith("ANNULATION")) {
                    texte = Theme.DANGER;
                } else if ("En attente".equals(s) || s.startsWith("RETRAIT (DECOUVERT)") || s.startsWith("FRAIS")) {
                    texte = Theme.AVERTISSEMENT;
                }
            }
            l.setForeground(texte);
            if (isSelected) {
                l.setBackground(table.getSelectionBackground());
            } else {
                l.setBackground(row % 2 == 0 ? Color.WHITE : Theme.LIGNE_ALTERNEE);
            }
            l.setToolTipText(affiche == null ? null : affiche.toString());
            return l;
        }
    }
}
