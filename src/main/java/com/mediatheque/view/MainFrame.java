package com.mediatheque.view;

import com.mediatheque.controller.Session;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Fenêtre principale de l'application : navigation latérale (sidebar) et
 * affichage des modules métier dans une zone centrale (CardLayout).
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final List<JButton> boutonsNav = new ArrayList<>();

    public MainFrame() {
        setTitle("Médiathèque de Bourg-la-Reine - Gestion (Client Lourd)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(creerEntete(), BorderLayout.NORTH);
        add(creerSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.add(new DashboardPanel(), "Accueil");
        contentPanel.add(new ClientPanel(), "Clients");
        contentPanel.add(new TechnicienPanel(), "Techniciens");
        contentPanel.add(new SallePanel(), "Salles");
        contentPanel.add(new AnimationPanel(), "Animations");
        contentPanel.add(new ReservationPanel(), "Reservations");
        contentPanel.add(new FacturePanel(), "Factures");
        if (Session.estAdmin()) {
            contentPanel.add(new ProfilPanel(), "Profils");
        }
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel creerEntete() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(UITheme.PRIMARY_DK);
        entete.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titre = new JLabel("Médiathèque de Bourg-la-Reine");
        titre.setForeground(Color.black);
        titre.setFont(UITheme.H2);
        entete.add(titre, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);
        JLabel user = new JLabel("Connecté : " + Session.getProfil().toString());
        user.setForeground(Color.black);
        droite.add(user);

        JButton deconnexion = new JButton("Déconnexion");
        UITheme.styleAccentButton(deconnexion);
        deconnexion.addActionListener(e -> deconnecter());
        droite.add(deconnexion);

        entete.add(droite, BorderLayout.EAST);
        return entete;
    }

    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        ajouterBouton(sidebar, "Accueil", "Accueil");
        ajouterBouton(sidebar, "Clients", "Clients");
        ajouterBouton(sidebar, "Techniciens", "Techniciens");
        ajouterBouton(sidebar, "Salles", "Salles");
        ajouterBouton(sidebar, "Animations", "Animations");
        ajouterBouton(sidebar, "Réservations", "Reservations");
        ajouterBouton(sidebar, "Factures", "Factures");
        if (Session.estAdmin()) {
            ajouterBouton(sidebar, "Profils", "Profils");
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void ajouterBouton(JPanel sidebar, String libelle, String carte) {
        JButton b = new JButton(libelle);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(UITheme.PRIMARY);
        b.setForeground(Color.black);
        b.setFont(new Font("SansSerif", Font.PLAIN, 15));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 8));
        b.addActionListener(e -> {
            cardLayout.show(contentPanel, carte);
            surligner(b);
        });
        boutonsNav.add(b);
        sidebar.add(b);
    }

    private void surligner(JButton actif) {
        for (JButton b : boutonsNav) {
            b.setBackground(b == actif ? UITheme.PRIMARY_DK : UITheme.PRIMARY);
        }
    }

    private void deconnecter() {
        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter ?", "Déconnexion",
                JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            Session.fermer();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
