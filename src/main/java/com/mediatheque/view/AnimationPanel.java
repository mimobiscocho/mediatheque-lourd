package com.mediatheque.view;

import com.mediatheque.controller.AnimationController;
import com.mediatheque.model.Animation;
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
 * Module de gestion des animations.
 */
public class AnimationPanel extends JPanel {

    private final AnimationController controller = new AnimationController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Titre", "Date", "Début", "Fin", "Places", "Salle", "Technicien"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Animation> donnees;

    public AnimationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(UITheme.title("Gestion des animations"), BorderLayout.NORTH);
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
                    "Impossible de charger les animations : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        model.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        for (Animation a : donnees) {
            model.addRow(new Object[]{a.getId(), a.getTitre(),
                    a.getDateAnimation() == null ? "" : a.getDateAnimation().format(df),
                    a.getHeureDebut()    == null ? "" : a.getHeureDebut().format(tf),
                    a.getHeureFin()      == null ? "" : a.getHeureFin().format(tf),
                    a.getNbPlaces(), a.getSalleNom(), a.getTechnicienNom()});
        }
    }

    private Animation selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une animation.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Animation a = selection();
        if (a != null) {
            ouvrirFormulaire(a);
        }
    }

    private void ouvrirFormulaire(Animation a) {
        AnimationDialog dialog = new AnimationDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), controller, a);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Animation a = selection();
        if (a == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer l'animation « " + a.getTitre() + " » ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(a.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
