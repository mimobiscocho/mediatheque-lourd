package com.mediatheque.view;

import com.mediatheque.controller.ProfilController;
import com.mediatheque.model.Profil;
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
 * Module de gestion des profils (agents) - réservé aux administrateurs.
 */
public class ProfilPanel extends JPanel {

    private final ProfilController controller = new ProfilController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Login", "Nom", "Prénom", "Rôle"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Profil> donnees;

    public ProfilPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(UITheme.title("Gestion des profils"), BorderLayout.NORTH);
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
        for (Profil p : donnees) {
            model.addRow(new Object[]{p.getId(), p.getLogin(), p.getNom(), p.getPrenom(), p.getRole()});
        }
    }

    private Profil selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un profil.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Profil p = selection();
        if (p != null) {
            ouvrirFormulaire(p);
        }
    }

    private void ouvrirFormulaire(Profil p) {
        ProfilDialog dialog = new ProfilDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), controller, p);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Profil p = selection();
        if (p == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer le profil « " + p.getLogin() + " » ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(p.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
