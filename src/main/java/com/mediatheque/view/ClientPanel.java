package com.mediatheque.view;

import com.mediatheque.controller.ClientController;
import com.mediatheque.model.Client;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Module de gestion des clients (adhérents) : liste, recherche et CRUD.
 */
public class ClientPanel extends JPanel {

    private final ClientController controller = new ClientController();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nom", "Prénom", "Email", "Téléphone", "Abonnement", "Inscription", "Actif"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField rechercheField = new JTextField(20);
    private List<Client> donnees;

    public ClientPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(creerEntete(), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(creerBarreBoutons(), BorderLayout.SOUTH);

        rafraichir();
    }

    private JPanel creerEntete() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(UITheme.title("Gestion des clients"), BorderLayout.WEST);

        JPanel recherche = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        recherche.setOpaque(false);
        recherche.add(new JLabel("Rechercher :"));
        recherche.add(rechercheField);
        JButton btn = new JButton("OK");
        UITheme.stylePrimaryButton(btn);
        btn.addActionListener(e -> rechercher());
        rechercheField.addActionListener(e -> rechercher());
        recherche.add(btn);
        JButton reset = new JButton("Tout");
        reset.addActionListener(e -> {
            rechercheField.setText("");
            rafraichir();
        });
        recherche.add(reset);
        entete.add(recherche, BorderLayout.EAST);
        return entete;
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
        peupler();
    }

    private void rechercher() {
        donnees = controller.rechercher(rechercheField.getText());
        peupler();
    }

    private void peupler() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Client c : donnees) {
            model.addRow(new Object[]{
                    c.getId(), c.getNom(), c.getPrenom(), c.getEmail(), c.getTelephone(),
                    c.getTypeAbonnement(),
                    c.getDateInscription() == null ? "" : c.getDateInscription().format(fmt),
                    c.isActif() ? "Oui" : "Non"});
        }
    }

    private Client selection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client.");
            return null;
        }
        return donnees.get(table.convertRowIndexToModel(row));
    }

    private void modifierSelection() {
        Client c = selection();
        if (c != null) {
            ouvrirFormulaire(c);
        }
    }

    private void ouvrirFormulaire(Client client) {
        ClientDialog dialog = new ClientDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), controller, client);
        dialog.setVisible(true);
        if (dialog.estValide()) {
            rafraichir();
        }
    }

    private void supprimerSelection() {
        Client c = selection();
        if (c == null) {
            return;
        }
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer le client « " + c + " » ?\n(Ses factures et réservations seront aussi supprimées.)",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                controller.supprimer(c.getId());
                rafraichir();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
