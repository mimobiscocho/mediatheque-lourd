package com.mediatheque.view;

import com.mediatheque.controller.ClientController;
import com.mediatheque.controller.FactureController;
import com.mediatheque.model.Client;
import com.mediatheque.model.Facture;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Boîte de dialogue de saisie / modification d'une facture.
 */
public class FactureDialog extends JDialog {

    private final FactureController controller;
    private final Facture facture;
    private boolean valide = false;

    private final JComboBox<Client> clientCombo = new JComboBox<>();
    private final JTextField libelleField = new JTextField(20);
    private final JTextField montantField = new JTextField(20);
    private final JTextField dateField = new JTextField(20);
    private final JComboBox<String> statutCombo = new JComboBox<>(new String[]{"IMPAYEE", "PAYEE"});

    public FactureDialog(Frame parent, FactureController controller, Facture facture) {
        super(parent, true);
        this.controller = controller;
        this.facture = facture != null ? facture : new Facture();
        setTitle(facture != null ? "Modifier une facture" : "Nouvelle facture");
        setSize(440, 340);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        for (Client c : new ClientController().lister()) {
            clientCombo.addItem(c);
        }
        add(creerFormulaire(), BorderLayout.CENTER);
        add(creerBoutons(), BorderLayout.SOUTH);
        remplirChamps();
    }

    private JPanel creerFormulaire() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        ligne(p, g, y++, "Client *", clientCombo);
        ligne(p, g, y++, "Libellé *", libelleField);
        ligne(p, g, y++, "Montant (€) *", montantField);
        ligne(p, g, y++, "Date (jj/mm/aaaa) *", dateField);
        ligne(p, g, y++, "Statut", statutCombo);
        return p;
    }

    private void ligne(JPanel p, GridBagConstraints g, int y, String label, java.awt.Component champ) {
        g.gridx = 0;
        g.gridy = y;
        g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(champ, g);
    }

    private JPanel creerBoutons() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 14));
        JButton enregistrer = new JButton("Enregistrer");
        JButton annuler = new JButton("Annuler");
        UITheme.stylePrimaryButton(enregistrer);
        enregistrer.addActionListener(e -> enregistrer());
        annuler.addActionListener(e -> dispose());
        p.add(enregistrer);
        p.add(annuler);
        return p;
    }

    private void remplirChamps() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        selectionnerClient(facture.getClientId());
        libelleField.setText(facture.getLibelle());
        montantField.setText(facture.getMontant() == null ? "0.00" : facture.getMontant().toPlainString());
        dateField.setText(facture.getDateEmission() == null
                ? LocalDate.now().format(df) : facture.getDateEmission().format(df));
        statutCombo.setSelectedItem(facture.getStatut());
    }

    private void selectionnerClient(int id) {
        for (int i = 0; i < clientCombo.getItemCount(); i++) {
            if (clientCombo.getItemAt(i).getId() == id) {
                clientCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void enregistrer() {
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Client client = (Client) clientCombo.getSelectedItem();
            if (client == null) {
                throw new IllegalArgumentException("Veuillez d'abord créer un client.");
            }
            facture.setClientId(client.getId());
            facture.setLibelle(libelleField.getText().trim());
            facture.setMontant(new BigDecimal(montantField.getText().trim().replace(',', '.')));
            facture.setDateEmission(LocalDate.parse(dateField.getText().trim(), df));
            facture.setStatut((String) statutCombo.getSelectedItem());

            controller.enregistrer(facture);
            valide = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date invalide (jj/mm/aaaa).", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean estValide() {
        return valide;
    }
}
