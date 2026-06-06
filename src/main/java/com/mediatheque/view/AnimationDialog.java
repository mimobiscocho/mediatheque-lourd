package com.mediatheque.view;

import com.mediatheque.controller.AnimationController;
import com.mediatheque.controller.SalleController;
import com.mediatheque.controller.TechnicienController;
import com.mediatheque.model.Animation;
import com.mediatheque.model.Salle;
import com.mediatheque.model.Technicien;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Boîte de dialogue de saisie / modification d'une animation.
 */
public class AnimationDialog extends JDialog {

    private final AnimationController controller;
    private final Animation animation;
    private boolean valide = false;

    private final JTextField titreField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(3, 20);
    private final JTextField dateField = new JTextField(20);
    private final JTextField debutField = new JTextField(20);
    private final JTextField finField = new JTextField(20);
    private final JSpinner placesSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JComboBox<Salle> salleCombo = new JComboBox<>();
    private final JComboBox<Technicien> technicienCombo = new JComboBox<>();

    public AnimationDialog(Frame parent, AnimationController controller, Animation animation) {
        super(parent, true);
        this.controller = controller;
        this.animation = animation != null ? animation : new Animation();
        setTitle(animation != null ? "Modifier une animation" : "Nouvelle animation");
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        chargerCombos();
        add(creerFormulaire(), BorderLayout.CENTER);
        add(creerBoutons(), BorderLayout.SOUTH);
        remplirChamps();
    }

    private void chargerCombos() {
        int salleCouranteId = animation.getSalleId();
        for (Salle s : new SalleController().lister()) {
            // En création, on n'affiche que les salles disponibles.
            // En modification, on conserve la salle déjà liée même si elle a
            // été marquée indisponible depuis (sinon le combo serait vide).
            if (s.isDisponible() || s.getId() == salleCouranteId) {
                salleCombo.addItem(s);
            }
        }
        for (Technicien t : new TechnicienController().lister()) {
            technicienCombo.addItem(t);
        }
    }

    private JPanel creerFormulaire() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        ligne(p, g, y++, "Titre *", titreField);
        descriptionArea.setLineWrap(true);
        ligne(p, g, y++, "Description", new JScrollPane(descriptionArea));
        ligne(p, g, y++, "Date (jj/mm/aaaa) *", dateField);
        ligne(p, g, y++, "Heure début (HH:mm) *", debutField);
        ligne(p, g, y++, "Heure fin (HH:mm) *", finField);
        ligne(p, g, y++, "Nombre de places", placesSpinner);
        ligne(p, g, y++, "Salle *", salleCombo);
        ligne(p, g, y++, "Technicien *", technicienCombo);
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
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        titreField.setText(animation.getTitre());
        descriptionArea.setText(animation.getDescription());
        dateField.setText(animation.getDateAnimation() == null
                ? LocalDate.now().format(df) : animation.getDateAnimation().format(df));
        debutField.setText(animation.getHeureDebut() == null ? "14:00" : animation.getHeureDebut().format(tf));
        finField.setText(animation.getHeureFin() == null ? "16:00" : animation.getHeureFin().format(tf));
        placesSpinner.setValue(animation.getNbPlaces());
        selectionnerSalle(animation.getSalleId());
        selectionnerTechnicien(animation.getTechnicienId());
    }

    private void selectionnerSalle(int id) {
        for (int i = 0; i < salleCombo.getItemCount(); i++) {
            if (salleCombo.getItemAt(i).getId() == id) {
                salleCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectionnerTechnicien(int id) {
        for (int i = 0; i < technicienCombo.getItemCount(); i++) {
            if (technicienCombo.getItemAt(i).getId() == id) {
                technicienCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void enregistrer() {
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            Salle salle = (Salle) salleCombo.getSelectedItem();
            Technicien tech = (Technicien) technicienCombo.getSelectedItem();
            if (salle == null || tech == null) {
                throw new IllegalArgumentException("Veuillez créer au moins une salle et un technicien.");
            }

            animation.setTitre(titreField.getText().trim());
            animation.setDescription(descriptionArea.getText().trim());
            animation.setDateAnimation(LocalDate.parse(dateField.getText().trim(), df));
            animation.setHeureDebut(LocalTime.parse(debutField.getText().trim(), tf));
            animation.setHeureFin(LocalTime.parse(finField.getText().trim(), tf));
            animation.setNbPlaces((Integer) placesSpinner.getValue());
            animation.setSalleId(salle.getId());
            animation.setTechnicienId(tech.getId());

            controller.enregistrer(animation);
            valide = true;
            dispose();
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Format de date ou d'heure invalide.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean estValide() {
        return valide;
    }
}
