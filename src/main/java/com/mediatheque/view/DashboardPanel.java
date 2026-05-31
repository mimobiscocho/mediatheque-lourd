package com.mediatheque.view;

import com.mediatheque.controller.AnimationController;
import com.mediatheque.controller.ClientController;
import com.mediatheque.controller.FactureController;
import com.mediatheque.controller.ReservationController;
import com.mediatheque.controller.SalleController;
import com.mediatheque.controller.TechnicienController;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Tableau de bord d'accueil affichant des indicateurs synthétiques.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titre = UITheme.title("Tableau de bord");
        add(titre, BorderLayout.NORTH);

        JPanel grille = new JPanel(new GridLayout(2, 3, 20, 20));
        grille.setOpaque(false);
        try {
            grille.add(carte("Clients", new ClientController().lister().size(), UITheme.PRIMARY));
            grille.add(carte("Techniciens", new TechnicienController().lister().size(), UITheme.ACCENT));
            grille.add(carte("Salles", new SalleController().lister().size(), UITheme.SUCCESS));
            grille.add(carte("Animations", new AnimationController().lister().size(), UITheme.PRIMARY));
            grille.add(carte("Réservations", new ReservationController().lister().size(), UITheme.ACCENT));
            grille.add(carte("Factures", new FactureController().lister().size(), UITheme.SUCCESS));
        } catch (RuntimeException ex) {
            JLabel err = new JLabel("<html>Connexion à la base impossible :<br>" + ex.getMessage() + "</html>");
            err.setForeground(UITheme.DANGER);
            grille.add(err);
        }
        add(grille, BorderLayout.CENTER);

        JLabel pied = new JLabel("Application de gestion - SEBAH Nassim - BTS SIO SLAM 2026",
                SwingConstants.CENTER);
        pied.setForeground(new Color(0x888888));
        add(pied, BorderLayout.SOUTH);
    }

    private JPanel carte(String libelle, int valeur, Color couleur) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(UITheme.WHITE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, couleur),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        carte.setPreferredSize(new Dimension(200, 120));

        JLabel nombre = new JLabel(String.valueOf(valeur));
        nombre.setFont(new Font("SansSerif", Font.BOLD, 40));
        nombre.setForeground(couleur);

        JLabel nom = new JLabel(libelle);
        nom.setFont(UITheme.H2);
        nom.setForeground(UITheme.TEXT);

        carte.add(nombre, BorderLayout.CENTER);
        carte.add(nom, BorderLayout.SOUTH);
        return carte;
    }
}
