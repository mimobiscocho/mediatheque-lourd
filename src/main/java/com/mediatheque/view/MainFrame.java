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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        setTitle("Médiathèque de Bourg-la-Reine — Gestion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1240, 760);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        add(creerEntete(),  BorderLayout.NORTH);
        add(creerSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.add(new DashboardPanel(),  "Accueil");
        contentPanel.add(new ClientPanel(),     "Clients");
        contentPanel.add(new TechnicienPanel(), "Techniciens");
        contentPanel.add(new SallePanel(),      "Salles");
        contentPanel.add(new AnimationPanel(),  "Animations");
        contentPanel.add(new ReservationPanel(),"Reservations");
        contentPanel.add(new FacturePanel(),    "Factures");
        if (Session.estAdmin()) {
            contentPanel.add(new ProfilPanel(), "Profils");
        }
        add(contentPanel, BorderLayout.CENTER);

        // Active la première entrée de menu par défaut
        if (!boutonsNav.isEmpty()) {
            surligner(boutonsNav.get(0));
        }
    }

    // ------------------------------------------------------------------
    //  En-tête (barre du haut)
    // ------------------------------------------------------------------
    private JPanel creerEntete() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(UITheme.SURFACE);
        entete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        JLabel logo = new JLabel("MB");
        logo.setOpaque(true);
        logo.setBackground(UITheme.PRIMARY);
        logo.setForeground(UITheme.WHITE);
        logo.setFont(new Font(UITheme.TITLE.getFamily(), Font.BOLD, 14));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));
        gauche.add(logo);

        JLabel titre = new JLabel("Médiathèque de Bourg-la-Reine");
        titre.setForeground(UITheme.TEXT);
        titre.setFont(UITheme.H2);
        gauche.add(titre);

        entete.add(gauche, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);

        JLabel role = UITheme.badge(
                Session.getProfil().getRole(),
                UITheme.PRIMARY_SOFT, UITheme.PRIMARY);
        droite.add(role);

        JLabel user = new JLabel(Session.getProfil().getPrenom()
                + " " + Session.getProfil().getNom());
        user.setForeground(UITheme.TEXT);
        user.setFont(UITheme.NORMAL);
        droite.add(user);

        JButton deconnexion = new JButton("Déconnexion");
        UITheme.styleGhostButton(deconnexion);
        UITheme.clickable(deconnexion);
        deconnexion.addActionListener(e -> deconnecter());
        droite.add(deconnexion);

        entete.add(droite, BorderLayout.EAST);
        return entete;
    }

    // ------------------------------------------------------------------
    //  Sidebar (navigation gauche)
    // ------------------------------------------------------------------
    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SURFACE);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER),
                BorderFactory.createEmptyBorder(20, 16, 20, 16)));

        sidebar.add(sectionLabel("MENU"));

        ajouterBouton(sidebar, "Tableau de bord", "Accueil");
        ajouterBouton(sidebar, "Clients",         "Clients");
        ajouterBouton(sidebar, "Techniciens",     "Techniciens");
        ajouterBouton(sidebar, "Salles",          "Salles");
        ajouterBouton(sidebar, "Animations",      "Animations");
        ajouterBouton(sidebar, "Réservations",    "Reservations");
        ajouterBouton(sidebar, "Factures",        "Factures");

        if (Session.estAdmin()) {
            sidebar.add(Box.createVerticalStrut(18));
            sidebar.add(sectionLabel("ADMINISTRATION"));
            ajouterBouton(sidebar, "Profils", "Profils");
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JLabel sectionLabel(String libelle) {
        JLabel l = new JLabel(libelle);
        l.setFont(UITheme.SMALL.deriveFont(Font.BOLD));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void ajouterBouton(JPanel sidebar, String libelle, String carte) {
        JButton b = new JButton(libelle);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(UITheme.SURFACE);
        b.setForeground(UITheme.TEXT);
        b.setFont(UITheme.NORMAL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        UITheme.clickable(b);

        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (b.getClientProperty("active") != Boolean.TRUE) {
                    b.setBackground(UITheme.ROW_HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (b.getClientProperty("active") != Boolean.TRUE) {
                    b.setBackground(UITheme.SURFACE);
                }
            }
        });

        b.addActionListener(e -> {
            cardLayout.show(contentPanel, carte);
            surligner(b);
        });
        boutonsNav.add(b);
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(2));
    }

    private void surligner(JButton actif) {
        for (JButton b : boutonsNav) {
            boolean isActive = (b == actif);
            b.putClientProperty("active", isActive);
            b.setBackground(isActive ? UITheme.PRIMARY_SOFT : UITheme.SURFACE);
            b.setForeground(isActive ? UITheme.PRIMARY     : UITheme.TEXT);
            b.setFont(isActive ? UITheme.NORMAL.deriveFont(Font.BOLD) : UITheme.NORMAL);
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
