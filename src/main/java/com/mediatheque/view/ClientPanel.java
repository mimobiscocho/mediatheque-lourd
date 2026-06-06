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
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(creerEntete(), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(UITheme.wrapTable(table), BorderLayout.CENTER);
        add(creerBarreBoutons(), BorderLayout.SOUTH);

        rafraichir();
    }

    private JPanel creerEntete() {
        JPanel entete = new JPanel(new BorderLayout(0, 0));
        entete.setOpaque(false);

        JPanel gauche = new JPanel();
        gauche.setOpaque(false);
        gauche.setLayout(new javax.swing.BoxLayout(gauche, javax.swing.BoxLayout.Y_AXIS));
        gauche.add(UITheme.title("Clients"));
        JLabel sous = UITheme.subtitle("Adhérents de la médiathèque — recherche par nom, prénom ou email.");
        sous.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        gauche.add(sous);
        entete.add(gauche, BorderLayout.WEST);

        JPanel recherche = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        recherche.setOpaque(false);
        rechercheField.setFont(UITheme.NORMAL);
        rechercheField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        recherche.add(rechercheField);
        JButton btn = new JButton("Rechercher");
        UITheme.stylePrimaryButton(btn);
        btn.addActionListener(e -> rechercher());
        rechercheField.addActionListener(e -> rechercher());
        recherche.add(btn);
        JButton reset = new JButton("Effacer");
        UITheme.styleGhostButton(reset);
        reset.addActionListener(e -> {
            rechercheField.setText("");
            rafraichir();
        });
        recherche.add(reset);
        entete.add(recherche, BorderLayout.EAST);
        return entete;
    }

    private JPanel creerBarreBoutons() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barre.setOpaque(false);
        barre.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        JButton ajouter  = new JButton("+ Nouveau client");
        JButton modifier = new JButton("Modifier");
        JButton supprimer = new JButton("Supprimer");
        UITheme.stylePrimaryButton(ajouter);
        UITheme.styleGhostButton(modifier);
        UITheme.styleGhostButton(supprimer);
        supprimer.setForeground(UITheme.DANGER);
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
