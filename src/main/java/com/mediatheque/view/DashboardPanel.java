package com.mediatheque.view;

import com.mediatheque.controller.Session;
import com.mediatheque.dao.StatsDAO;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
        setLayout(new BorderLayout(0, 24));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        add(creerEntete(), BorderLayout.NORTH);
        add(creerCorps(),  BorderLayout.CENTER);
    }

    private JPanel creerEntete() {
        JPanel entete = new JPanel();
        entete.setOpaque(false);
        entete.setLayout(new BoxLayout(entete, BoxLayout.Y_AXIS));

        JLabel salut = new JLabel("Bonjour, " + Session.getProfil().getPrenom() + ".");
        salut.setFont(UITheme.TITLE);
        salut.setForeground(UITheme.TEXT);
        salut.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sous = new JLabel("Voici l'activité de la médiathèque aujourd'hui.");
        sous.setFont(UITheme.NORMAL);
        sous.setForeground(UITheme.TEXT_MUTED);
        sous.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        sous.setAlignmentX(LEFT_ALIGNMENT);

        entete.add(salut);
        entete.add(sous);
        return entete;
    }

    private JPanel creerCorps() {
        JPanel corps = new JPanel(new BorderLayout(0, 20));
        corps.setOpaque(false);

        JPanel grille = new JPanel(new GridLayout(2, 3, 18, 18));
        grille.setOpaque(false);

        try {
            Map<String, Integer> compteurs = new StatsDAO().compteurs();
            int i = 0;
            for (Map.Entry<String, Integer> e : compteurs.entrySet()) {
                grille.add(carte(e.getKey(), e.getValue(), PALETTE[i % PALETTE.length]));
                i++;
            }
        } catch (RuntimeException ex) {
            corps.removeAll();
            JPanel err = UITheme.card();
            err.setLayout(new BorderLayout());
            JLabel msg = new JLabel("<html><div style='text-align:center;'>"
                    + "<b>Connexion à la base impossible.</b><br>"
                    + "<span style='color:#64748B;'>Vérifiez que MySQL est démarré.</span></div></html>",
                    SwingConstants.CENTER);
            msg.setFont(UITheme.NORMAL);
            err.add(msg, BorderLayout.CENTER);
            corps.add(err, BorderLayout.NORTH);
            return corps;
        }

        corps.add(grille, BorderLayout.NORTH);

        // Section "À propos"
        JPanel about = UITheme.card();
        about.setLayout(new BoxLayout(about, BoxLayout.Y_AXIS));
        JLabel aTitre = new JLabel("À propos de l'application");
        aTitre.setFont(UITheme.H2);
        aTitre.setForeground(UITheme.TEXT);
        aTitre.setAlignmentX(LEFT_ALIGNMENT);
        JLabel aTexte = new JLabel("<html><div style='color:#64748B;'>"
                + "Application de gestion de la médiathèque (réservations, animations,<br>"
                + "facturation). Base de données partagée avec le client web."
                + "</div></html>");
        aTexte.setFont(UITheme.NORMAL);
        aTexte.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        aTexte.setAlignmentX(LEFT_ALIGNMENT);
        about.add(aTitre);
        about.add(Box.createVerticalStrut(4));
        about.add(aTexte);

        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setOpaque(false);
        southWrap.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        southWrap.add(about, BorderLayout.NORTH);

        JLabel pied = new JLabel("Médiathèque de Bourg-la-Reine — SEBAH Nassim — BTS SIO SLAM 2026",
                SwingConstants.CENTER);
        pied.setForeground(UITheme.TEXT_MUTED);
        pied.setFont(UITheme.SMALL);
        pied.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        southWrap.add(pied, BorderLayout.SOUTH);

        corps.add(southWrap, BorderLayout.CENTER);

        return corps;
    }

    private JPanel carte(String libelle, int valeur, Color couleur) {
        JPanel carte = new JPanel(new BorderLayout(0, 6));
        carte.setBackground(UITheme.SURFACE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(3, 0, 0, 0, couleur),
                        BorderFactory.createEmptyBorder(20, 22, 20, 22))));
        carte.setPreferredSize(new Dimension(200, 130));

        JLabel nom = new JLabel(libelle.toUpperCase());
        nom.setFont(UITheme.SMALL.deriveFont(Font.BOLD));
        nom.setForeground(UITheme.TEXT_MUTED);

        JLabel nombre = new JLabel(String.valueOf(valeur));
        nombre.setFont(new Font(UITheme.TITLE.getFamily(), Font.BOLD, 38));
        nombre.setForeground(UITheme.TEXT);

        carte.add(nom,    BorderLayout.NORTH);
        carte.add(nombre, BorderLayout.CENTER);
        return carte;
    }
}
