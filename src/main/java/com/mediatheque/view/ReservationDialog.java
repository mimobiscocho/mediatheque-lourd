package com.mediatheque.view;

import com.mediatheque.controller.ClientController;
import com.mediatheque.controller.ReservationController;
import com.mediatheque.controller.SalleController;
import com.mediatheque.model.Client;
import com.mediatheque.model.Reservation;
import com.mediatheque.model.Salle;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
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
 * Boîte de dialogue de saisie / modification d'une réservation.
 * Les contrôles de disponibilité sont assurés côté base (triggers).
 */
public class ReservationDialog extends JDialog {

    private final ReservationController controller;
    private final Reservation reservation;
    private boolean valide = false;

    private final JComboBox<Client> clientCombo = new JComboBox<>();
    private final JComboBox<Salle> salleCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(20);
    private final JTextField debutField = new JTextField(20);
    private final JTextField finField = new JTextField(20);
    private final JComboBox<String> statutCombo = new JComboBox<>(new String[]{"CONFIRMEE", "ANNULEE"});

    public ReservationDialog(Frame parent, ReservationController controller, Reservation reservation) {
        super(parent, true);
        this.controller = controller;
        this.reservation = reservation != null ? reservation : new Reservation();
        setTitle(reservation != null ? "Modifier une réservation" : "Nouvelle réservation");
        setSize(460, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        chargerCombos();
        add(creerFormulaire(), BorderLayout.CENTER);
        add(creerBoutons(), BorderLayout.SOUTH);
        remplirChamps();
    }

    private void chargerCombos() {
        for (Client c : new ClientController().lister()) {
            clientCombo.addItem(c);
        }
        for (Salle s : new SalleController().lister()) {
            salleCombo.addItem(s);
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
        ligne(p, g, y++, "Client *", clientCombo);
        ligne(p, g, y++, "Salle *", salleCombo);
        ligne(p, g, y++, "Date (jj/mm/aaaa) *", dateField);
        ligne(p, g, y++, "Heure début (HH:mm) *", debutField);
        ligne(p, g, y++, "Heure fin (HH:mm) *", finField);
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
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        selectionnerClient(reservation.getClientId());
        selectionnerSalle(reservation.getSalleId());
        dateField.setText(reservation.getDateReservation() == null
                ? LocalDate.now().format(df) : reservation.getDateReservation().format(df));
        debutField.setText(reservation.getHeureDebut() == null ? "09:00" : reservation.getHeureDebut().format(tf));
        finField.setText(reservation.getHeureFin() == null ? "11:00" : reservation.getHeureFin().format(tf));
        statutCombo.setSelectedItem(reservation.getStatut());
    }

    private void selectionnerClient(int id) {
        for (int i = 0; i < clientCombo.getItemCount(); i++) {
            if (clientCombo.getItemAt(i).getId() == id) {
                clientCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectionnerSalle(int id) {
        for (int i = 0; i < salleCombo.getItemCount(); i++) {
            if (salleCombo.getItemAt(i).getId() == id) {
                salleCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void enregistrer() {
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            Client client = (Client) clientCombo.getSelectedItem();
            Salle salle = (Salle) salleCombo.getSelectedItem();
            if (client == null || salle == null) {
                throw new IllegalArgumentException("Veuillez créer au moins un client et une salle.");
            }

            reservation.setClientId(client.getId());
            reservation.setSalleId(salle.getId());
            reservation.setDateReservation(LocalDate.parse(dateField.getText().trim(), df));
            reservation.setHeureDebut(LocalTime.parse(debutField.getText().trim(), tf));
            reservation.setHeureFin(LocalTime.parse(finField.getText().trim(), tf));
            reservation.setStatut((String) statutCombo.getSelectedItem());

            controller.enregistrer(reservation);
            valide = true;
            dispose();
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Format de date ou d'heure invalide.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Réservation impossible",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean estValide() {
        return valide;
    }
}
