package com.mediatheque.view;

import com.mediatheque.controller.ReservationController;
import com.mediatheque.model.Reservation;
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
 * Module de gestion des réservations de salles.
 */
public class ReservationPanel extends JPanel {

    private final ReservationController controller = new ReservationController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Client", "Salle", "Date", "Début", "Fin", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Reservation> donnees;

    public ReservationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(UITheme.title("Gestion des réservations de salles"), BorderLayout.NORTH);
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

    private void rafraichir() {
        try {
            donnees = controller.lister();
        } catch (RuntimeException ex) {
            donnees = java.util.Collections.emptyList();
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les réservations : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        model.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        for (Reservation r : donnees) {
            model.addRow(new Object[]{r.getId(), r.getClientNom(), r.getSalleNom(),
                    r.getDateReservation() == null ? "" : r.getDateReservation().format(df),
                    r.getHeureDebut()      == null ? "" : r.getHeureDebut().format(tf),
                    r.getHeureFin()        == null ? "" : r.getHeureFin().format(tf),
                    r.getStatut()});
        }
    }

    private Reservation selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réservation.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Reservation r = selection();
        if (r != null) {
            ouvrirFormulaire(r);
        }
    }

    private void ouvrirFormulaire(Reservation r) {
        ReservationDialog dialog = new ReservationDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), controller, r);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Reservation r = selection();
        if (r == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer cette réservation ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(r.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
