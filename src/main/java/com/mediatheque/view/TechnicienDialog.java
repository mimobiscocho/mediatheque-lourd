package com.mediatheque.view;

import com.mediatheque.controller.TechnicienController;
import com.mediatheque.model.Technicien;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Boîte de dialogue de saisie / modification d'un technicien.
 */
public class TechnicienDialog extends JDialog {

    private final TechnicienController controller;
    private final Technicien technicien;
    private boolean valide = false;

    private final JTextField nomField = new JTextField(20);
    private final JTextField prenomField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField telField = new JTextField(20);
    private final JTextField specialiteField = new JTextField(20);

    public TechnicienDialog(Frame parent, TechnicienController controller, Technicien technicien) {
        super(parent, true);
        this.controller = controller;
        this.technicien = technicien != null ? technicien : new Technicien();
        setTitle(technicien != null ? "Modifier un technicien" : "Nouveau technicien");
        setSize(420, 320);
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
        g.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        ligne(p, g, y++, "Nom *", nomField);
        ligne(p, g, y++, "Prénom *", prenomField);
        ligne(p, g, y++, "Email", emailField);
        ligne(p, g, y++, "Téléphone", telField);
        ligne(p, g, y++, "Spécialité", specialiteField);
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
        nomField.setText(technicien.getNom());
        prenomField.setText(technicien.getPrenom());
        emailField.setText(technicien.getEmail());
        telField.setText(technicien.getTelephone());
        specialiteField.setText(technicien.getSpecialite());
    }

    private void enregistrer() {
        try {
            technicien.setNom(nomField.getText().trim());
            technicien.setPrenom(prenomField.getText().trim());
            technicien.setEmail(emailField.getText().trim());
            technicien.setTelephone(telField.getText().trim());
            technicien.setSpecialite(specialiteField.getText().trim());
            controller.enregistrer(technicien);
            valide = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean estValide() {
        return valide;
    }
}
