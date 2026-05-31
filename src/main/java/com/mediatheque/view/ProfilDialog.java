package com.mediatheque.view;

import com.mediatheque.controller.ProfilController;
import com.mediatheque.model.Profil;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Boîte de dialogue de saisie / modification d'un profil agent.
 */
public class ProfilDialog extends JDialog {

    private final ProfilController controller;
    private final Profil profil;
    private final boolean creation;
    private boolean valide = false;

    private final JTextField loginField = new JTextField(20);
    private final JTextField nomField = new JTextField(20);
    private final JTextField prenomField = new JTextField(20);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"AGENT", "ADMIN"});
    private final JPasswordField mdpField = new JPasswordField(20);

    public ProfilDialog(Frame parent, ProfilController controller, Profil profil) {
        super(parent, true);
        this.controller = controller;
        this.creation = (profil == null);
        this.profil = profil != null ? profil : new Profil();
        setTitle(creation ? "Nouveau profil" : "Modifier un profil");
        setSize(440, 320);
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
        ligne(p, g, y++, "Login *", loginField);
        ligne(p, g, y++, "Nom *", nomField);
        ligne(p, g, y++, "Prénom *", prenomField);
        ligne(p, g, y++, "Rôle", roleCombo);
        ligne(p, g, y++, creation ? "Mot de passe *" : "Nouveau mot de passe", mdpField);

        if (!creation) {
            g.gridx = 1;
            g.gridy = y;
            JLabel info = new JLabel("<html><i>Laisser vide pour ne pas changer.</i></html>");
            p.add(info, g);
        }
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
        loginField.setText(profil.getLogin());
        nomField.setText(profil.getNom());
        prenomField.setText(profil.getPrenom());
        if (profil.getRole() != null) {
            roleCombo.setSelectedItem(profil.getRole());
        }
    }

    private void enregistrer() {
        try {
            profil.setLogin(loginField.getText().trim());
            profil.setNom(nomField.getText().trim());
            profil.setPrenom(prenomField.getText().trim());
            profil.setRole((String) roleCombo.getSelectedItem());
            String mdp = new String(mdpField.getPassword());
            controller.enregistrer(profil, mdp);
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
