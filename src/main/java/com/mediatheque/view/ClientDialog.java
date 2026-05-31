package com.mediatheque.view;

import com.mediatheque.controller.ClientController;
import com.mediatheque.model.Client;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Boîte de dialogue de saisie / modification d'un client.
 */
public class ClientDialog extends JDialog {

    private final ClientController controller;
    private final Client client;
    private boolean valide = false;

    private final JTextField nomField = new JTextField(20);
    private final JTextField prenomField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField telField = new JTextField(20);
    private final JTextField adresseField = new JTextField(20);
    private final JComboBox<String> abonnementCombo =
            new JComboBox<>(new String[]{"STANDARD", "PREMIUM", "ETUDIANT"});
    private final JTextField dateField = new JTextField(20);
    private final JCheckBox actifCheck = new JCheckBox("Adhérent actif", true);

    public ClientDialog(Frame parent, ClientController controller, Client client) {
        super(parent, true);
        this.controller = controller;
        this.client = client != null ? client : new Client();
        setTitle(client != null ? "Modifier un client" : "Nouveau client");
        setSize(440, 460);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

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
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        ajouterLigne(p, g, y++, "Nom *", nomField);
        ajouterLigne(p, g, y++, "Prénom *", prenomField);
        ajouterLigne(p, g, y++, "Email", emailField);
        ajouterLigne(p, g, y++, "Téléphone", telField);
        ajouterLigne(p, g, y++, "Adresse", adresseField);
        ajouterLigne(p, g, y++, "Abonnement", abonnementCombo);
        ajouterLigne(p, g, y++, "Inscription (jj/mm/aaaa)", dateField);

        g.gridx = 1;
        g.gridy = y;
        p.add(actifCheck, g);
        return p;
    }

    private void ajouterLigne(JPanel p, GridBagConstraints g, int y, String label, java.awt.Component champ) {
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
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        nomField.setText(client.getNom());
        prenomField.setText(client.getPrenom());
        emailField.setText(client.getEmail());
        telField.setText(client.getTelephone());
        adresseField.setText(client.getAdresse());
        abonnementCombo.setSelectedItem(client.getTypeAbonnement());
        dateField.setText(client.getDateInscription() == null
                ? LocalDate.now().format(fmt) : client.getDateInscription().format(fmt));
        actifCheck.setSelected(client.isActif());
    }

    private void enregistrer() {
        try {
            client.setNom(nomField.getText().trim());
            client.setPrenom(prenomField.getText().trim());
            client.setEmail(emailField.getText().trim());
            client.setTelephone(telField.getText().trim());
            client.setAdresse(adresseField.getText().trim());
            client.setTypeAbonnement((String) abonnementCombo.getSelectedItem());
            client.setDateInscription(parseDate(dateField.getText().trim()));
            client.setActif(actifCheck.isSelected());

            controller.enregistrer(client);
            valide = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate parseDate(String texte) {
        try {
            return LocalDate.parse(texte, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Date d'inscription invalide (format jj/mm/aaaa).");
        }
    }

    public boolean estValide() {
        return valide;
    }
}
