package com.mediatheque.view;

import com.mediatheque.controller.SalleController;
import com.mediatheque.model.Salle;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Boîte de dialogue de saisie / modification d'une salle.
 */
public class SalleDialog extends JDialog {

    private final SalleController controller;
    private final Salle salle;
    private boolean valide = false;

    private final JTextField nomField = new JTextField(20);
    private final JSpinner capaciteSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 500, 1));
    private final JTextField equipementField = new JTextField(20);
    private final JCheckBox disponibleCheck = new JCheckBox("Salle disponible", true);

    public SalleDialog(Frame parent, SalleController controller, Salle salle) {
        super(parent, true);
        this.controller = controller;
        this.salle = salle != null ? salle : new Salle();
        setTitle(salle != null ? "Modifier une salle" : "Nouvelle salle");
        setSize(420, 280);
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

        ligne(p, g, 0, "Nom *", nomField);
        ligne(p, g, 1, "Capacité *", capaciteSpinner);
        ligne(p, g, 2, "Équipement", equipementField);
        g.gridx = 1;
        g.gridy = 3;
        p.add(disponibleCheck, g);
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
        nomField.setText(salle.getNom());
        capaciteSpinner.setValue(salle.getCapacite() > 0 ? salle.getCapacite() : 4);
        equipementField.setText(salle.getEquipement());
        disponibleCheck.setSelected(salle.isDisponible());
    }

    private void enregistrer() {
        try {
            salle.setNom(nomField.getText().trim());
            salle.setCapacite((Integer) capaciteSpinner.getValue());
            salle.setEquipement(equipementField.getText().trim());
            salle.setDisponible(disponibleCheck.isSelected());
            controller.enregistrer(salle);
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
