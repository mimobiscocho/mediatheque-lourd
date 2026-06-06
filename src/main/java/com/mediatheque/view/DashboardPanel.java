package com.mediatheque.view;

import com.mediatheque.dao.StatsDAO;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Tableau de bord d'accueil affichant des indicateurs synthétiques.
 * Une seule requête SQL agrégée alimente toutes les cartes.
 */
public class DashboardPanel extends JPanel {

    private static final Color[] PALETTE = {
            UITheme.PRIMARY, UITheme.ACCENT, UITheme.SUCCESS,
            UITheme.PRIMARY, UITheme.ACCENT, UITheme.SUCCESS
    };

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(UITheme.title("Tableau de bord"), BorderLayout.NORTH);
        add(creerGrille(), BorderLayout.CENTER);

        JLabel pied = new JLabel("Application de gestion - SEBAH Nassim - BTS SIO SLAM 2026",
                SwingConstants.CENTER);
        pied.setForeground(new Color(0x888888));
        add(pied, BorderLayout.SOUTH);
    }

    private JPanel creerGrille() {
        JPanel grille = new JPanel(new GridLayout(2, 3, 20, 20));
        grille.setOpaque(false);
        try {
            Map<String, Integer> compteurs = new StatsDAO().compteurs();
            int i = 0;
            for (Map.Entry<String, Integer> e : compteurs.entrySet()) {
                grille.add(carte(e.getKey(), e.getValue(), PALETTE[i % PALETTE.length]));
                i++;
            }
        } catch (RuntimeException ex) {
            JLabel err = new JLabel("<html>Connexion à la base impossible.<br>"
                    + "Vérifiez que MySQL est démarré.</html>", SwingConstants.CENTER);
            err.setForeground(UITheme.DANGER);
            grille.removeAll();
            grille.setLayout(new BorderLayout());
            grille.add(err, BorderLayout.CENTER);
        }
        return grille;
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
