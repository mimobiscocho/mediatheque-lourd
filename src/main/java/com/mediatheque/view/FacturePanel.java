package com.mediatheque.view;

import com.mediatheque.controller.FactureController;
import com.mediatheque.model.Facture;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
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
 * Module de gestion des factures.
 */
public class FacturePanel extends JPanel {

    private final FactureController controller = new FactureController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Client", "Libellé", "Montant (€)", "Date", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Facture> donnees;

    public FacturePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(UITheme.title("Gestion des factures"), BorderLayout.NORTH);
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
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Facture f : donnees) {
            model.addRow(new Object[]{f.getId(), f.getClientNom(), f.getLibelle(),
                    f.getMontant(), f.getDateEmission().format(df), f.getStatut()});
        }
    }

    private Facture selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une facture.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Facture f = selection();
        if (f != null) {
            ouvrirFormulaire(f);
        }
    }

    private void ouvrirFormulaire(Facture f) {
        FactureDialog dialog = new FactureDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), controller, f);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Facture f = selection();
        if (f == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer cette facture ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(f.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
