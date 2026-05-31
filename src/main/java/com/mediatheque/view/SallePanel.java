package com.mediatheque.view;

import com.mediatheque.controller.SalleController;
import com.mediatheque.model.Salle;
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
 * Module de gestion des salles de coworking.
 */
public class SallePanel extends JPanel {

    private final SalleController controller = new SalleController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nom", "Capacité", "Équipement", "Disponible"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Salle> donnees;

    public SallePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(UITheme.title("Gestion des salles"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
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

    private void rafraichir() {
        donnees = controller.lister();
        model.setRowCount(0);
        for (Salle s : donnees) {
            model.addRow(new Object[]{s.getId(), s.getNom(), s.getCapacite(),
                    s.getEquipement(), s.isDisponible() ? "Oui" : "Non"});
        }
    }

    private Salle selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une salle.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Salle s = selection();
        if (s != null) {
            ouvrirFormulaire(s);
        }
    }

    private void ouvrirFormulaire(Salle s) {
        SalleDialog dialog = new SalleDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), controller, s);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Salle s = selection();
        if (s == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer la salle « " + s.getNom() + " » ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(s.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
