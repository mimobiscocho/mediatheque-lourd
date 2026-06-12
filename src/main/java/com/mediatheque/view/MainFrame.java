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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Fenêtre principale de l'application, ouverte après la connexion.
 *
 * Organisation de l'écran :
 *  - en haut    : barre avec le nom de l'appli, l'agent connecté, Déconnexion
 *  - à gauche   : menu de navigation (un bouton par module)
 *  - au centre  : le module affiché, géré par un CardLayout
 *
 * Le CardLayout fonctionne comme une pile de "cartes" superposées :
 * chaque module est une carte, et cliquer sur un bouton du menu
 * fait passer la carte correspondante au premier plan.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    // On garde la liste des éléments du menu pour pouvoir
    // mettre en évidence celui qui est actif.
    private final List<NavItem> navItems = new ArrayList<>();

    public MainFrame() {
        setTitle("Médiathèque de Bourg-la-Reine — Gestion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(creerEntete(), BorderLayout.NORTH);
        add(creerMenu(), BorderLayout.WEST);

        // Une carte par module. Le nom passé en second argument est la clé
        // utilisée par cardLayout.show() pour retrouver la carte.
        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.add(new DashboardPanel(), "Accueil");
        contentPanel.add(new ClientPanel(), "Clients");
        contentPanel.add(new TechnicienPanel(), "Techniciens");
        contentPanel.add(new SallePanel(), "Salles");
        contentPanel.add(new AnimationPanel(), "Animations");
        contentPanel.add(new ReservationPanel(), "Reservations");
        contentPanel.add(new FacturePanel(), "Factures");
        // La gestion des profils n'existe que pour les administrateurs
        if (Session.estAdmin()) {
            contentPanel.add(new ProfilPanel(), "Profils");
        }
        add(contentPanel, BorderLayout.CENTER);

        // Au démarrage, le premier bouton (tableau de bord) est actif
        if (!navItems.isEmpty()) {
            surligner(navItems.get(0));
        }
    }

    /** Barre du haut : logo + titre à gauche, agent connecté et déconnexion à droite. */
    private JPanel creerEntete() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(UITheme.PRIMARY);
        entete.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.PRIMARY_FONCE),
                new EmptyBorder(14, 24, 14, 24)));

        // Bloc gauche : pastille « M » + titre + sous-titre
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        gauche.add(new LogoBadge());

        JPanel textes = new JPanel();
        textes.setOpaque(false);
        textes.setLayout(new BoxLayout(textes, BoxLayout.Y_AXIS));
        JLabel titre = new JLabel("Médiathèque de Bourg-la-Reine");
        titre.setForeground(UITheme.WHITE);
        titre.setFont(UITheme.H2);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sous = new JLabel("Application de gestion");
        sous.setForeground(UITheme.PRIMARY_CLAIR);
        sous.setFont(UITheme.SMALL);
        sous.setAlignmentX(Component.LEFT_ALIGNMENT);
        textes.add(titre);
        textes.add(sous);
        gauche.add(textes);

        entete.add(gauche, BorderLayout.WEST);

        // Bloc droit : prénom/nom (rôle) + bouton Déconnexion
        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        droite.setOpaque(false);

        JPanel infosUser = new JPanel();
        infosUser.setOpaque(false);
        infosUser.setLayout(new BoxLayout(infosUser, BoxLayout.Y_AXIS));
        JLabel nom = new JLabel(Session.getProfil().getPrenom() + " "
                + Session.getProfil().getNom(), SwingConstants.RIGHT);
        nom.setAlignmentX(Component.RIGHT_ALIGNMENT);
        nom.setForeground(UITheme.WHITE);
        nom.setFont(UITheme.NORMAL.deriveFont(Font.BOLD));
        JLabel role = new JLabel(Session.getProfil().getRole(), SwingConstants.RIGHT);
        role.setAlignmentX(Component.RIGHT_ALIGNMENT);
        role.setForeground(UITheme.PRIMARY_CLAIR);
        role.setFont(UITheme.SMALL);
        infosUser.add(nom);
        infosUser.add(role);
        droite.add(infosUser);

        JButton deconnexion = new JButton("Déconnexion");
        UITheme.styleGhostButton(deconnexion);
        deconnexion.addActionListener(e -> deconnecter());
        droite.add(deconnexion);

        entete.add(droite, BorderLayout.EAST);
        return entete;
    }

    /** Menu vertical de gauche : un bouton par module. */
    private JPanel creerMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(UITheme.SIDEBAR_BG);
        menu.setPreferredSize(new Dimension(230, 0));
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(20, 14, 20, 14)));

        ajouterRubrique(menu, "Navigation");
        ajouterBouton(menu, "Tableau de bord", "Accueil");
        ajouterBouton(menu, "Clients", "Clients");
        ajouterBouton(menu, "Techniciens", "Techniciens");
        ajouterBouton(menu, "Salles", "Salles");
        ajouterBouton(menu, "Animations", "Animations");
        ajouterBouton(menu, "Réservations", "Reservations");
        ajouterBouton(menu, "Factures", "Factures");

        // Section supplémentaire visible uniquement par les admins
        if (Session.estAdmin()) {
            menu.add(Box.createVerticalStrut(18));
            ajouterRubrique(menu, "Administration");
            ajouterBouton(menu, "Profils", "Profils");
        }

        // Pousse les boutons vers le haut du panneau
        menu.add(Box.createVerticalGlue());

        // Pied de menu (signature)
        JLabel signature = new JLabel("v1.0 — SEBAH N.");
        signature.setFont(UITheme.SMALL);
        signature.setForeground(UITheme.TEXT_MUTED);
        signature.setAlignmentX(Component.LEFT_ALIGNMENT);
        signature.setBorder(new EmptyBorder(6, 8, 0, 0));
        menu.add(signature);
        return menu;
    }

    /** Petit libellé d'en-tête de rubrique dans la sidebar (UPPERCASE muted). */
    private void ajouterRubrique(JPanel menu, String texte) {
        JLabel l = new JLabel(texte.toUpperCase());
        l.setFont(UITheme.SMALL_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(0, 8, 8, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(l);
    }

    /** Crée un bouton de menu et le relie à sa carte du CardLayout. */
    private void ajouterBouton(JPanel menu, String libelle, String carte) {
        NavItem item = new NavItem(libelle);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        item.addActionListener(() -> {
            cardLayout.show(contentPanel, carte);
            surligner(item);
        });

        navItems.add(item);
        menu.add(item);
        menu.add(Box.createVerticalStrut(2));
    }

    /** Met le bouton actif en évidence (barre verticale + fond clair). */
    private void surligner(NavItem actif) {
        for (NavItem n : navItems) {
            n.setActif(n == actif);
        }
    }

    /** Demande confirmation puis revient à l'écran de connexion. */
    private void deconnecter() {
        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter ?", "Déconnexion",
                JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            Session.fermer();   // oublie le profil connecté
            dispose();          // ferme la fenêtre principale
            new LoginFrame().setVisible(true);
        }
    }

    // ----- Composants internes ------------------------------------------

    /** Petite pastille verte foncée affichant la lettre « M » comme logo. */
    private static final class LogoBadge extends JComponent {
        LogoBadge() {
            setPreferredSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(UITheme.PRIMARY_FONCE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(UITheme.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            String s = "M";
            int sw = g2.getFontMetrics().stringWidth(s);
            int sh = g2.getFontMetrics().getAscent();
            g2.drawString(s, (getWidth() - sw) / 2,
                    (getHeight() + sh) / 2 - 3);
            g2.dispose();
        }
    }

    /** Élément de menu latéral : libellé + barre verticale verte si actif. */
    private static final class NavItem extends JComponent {
        private final String label;
        private boolean actif = false;
        private boolean hover = false;
        private Runnable action = () -> { };

        NavItem(String label) {
            this.label = label;
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setOpaque(false);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.run();
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void addActionListener(Runnable r) {
            this.action = r;
        }

        void setActif(boolean v) {
            this.actif = v;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fond (clair si actif, légèrement teinté si hover, transparent sinon)
            if (actif) {
                g2.setColor(UITheme.PRIMARY_CLAIR);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            } else if (hover) {
                g2.setColor(new Color(UITheme.PRIMARY.getRed(),
                        UITheme.PRIMARY.getGreen(),
                        UITheme.PRIMARY.getBlue(), 22));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            // Barre verticale verte à gauche si actif
            if (actif) {
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 6, 4, h - 12, 3, 3);
            }

            // Libellé
            g2.setFont(actif ? UITheme.MENU.deriveFont(Font.BOLD) : UITheme.MENU);
            g2.setColor(actif ? UITheme.PRIMARY_FONCE : UITheme.TEXT);
            int textX = 18;
            int baseline = (h + g2.getFontMetrics().getAscent()) / 2 - 3;
            g2.drawString(label, textX, baseline);

            g2.dispose();
        }
    }
}
