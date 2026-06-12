package com.mediatheque.view;

import com.mediatheque.controller.TechnicienController;
import com.mediatheque.model.Technicien;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Module de gestion des techniciens / animateurs.
 */
public class TechnicienPanel extends JPanel {

    private final TechnicienController controller = new TechnicienController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nom", "Prénom", "Email", "Téléphone", "Spécialité"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Technicien> donnees;

    public TechnicienPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(UITheme.title("Gestion des techniciens"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(UITheme.wrapTable(table), BorderLayout.CENTER);
        add(creerBarreBoutons(), BorderLayout.SOUTH);
        rafraichir();
    }

    private JPanel creerBarreBoutons() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barre.setOpaque(false);
        JButton ajouter = new JButton("Ajouter");
        JButton modifier = new JButton("Modifier");
        JButton supprimer = new JButton("Supprimer");
        UITheme.stylePrimaryButton(ajouter);
        UITheme.styleAccentButton(modifier);
        UITheme.styleDangerButton(supprimer);
        ajouter.addActionListener(e -> ouvrirFormulaire(null));
        modifier.addActionListener(e -> modifierSelection());
        supprimer.addActionListener(e -> supprimerSelection());
        barre.add(ajouter);
        barre.add(modifier);
        barre.add(supprimer);
        return barre;
    }

    /** Recharge la liste des techniciens depuis la base et remplit le tableau. */
    private void rafraichir() {
        try {
            donnees = controller.lister();
        } catch (RuntimeException ex) {
            donnees = java.util.Collections.emptyList();
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les techniciens : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        model.setRowCount(0);
        for (Technicien t : donnees) {
            model.addRow(new Object[]{t.getId(), t.getNom(), t.getPrenom(),
                    t.getEmail(), t.getTelephone(), t.getSpecialite()});
        }
    }

    private Technicien selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un technicien.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Technicien t = selection();
        if (t != null) {
            ouvrirFormulaire(t);
        }
    }

    private void ouvrirFormulaire(Technicien t) {
        TechnicienDialog dialog = new TechnicienDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), controller, t);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Technicien t = selection();
        if (t == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer le technicien « " + t + " » ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(t.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
