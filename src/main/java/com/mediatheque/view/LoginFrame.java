package com.mediatheque.view;

import com.mediatheque.controller.AuthController;
import com.mediatheque.model.Profil;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * Fenêtre de connexion : premier écran de l'application.
 * Tant que l'agent n'est pas authentifié, rien d'autre n'est accessible.
 * En cas de succès, on ferme cette fenêtre et on ouvre MainFrame.
 *
 * Mise en page : une carte blanche centrée sur un fond beige, avec un
 * bandeau vert en haut de la carte qui rappelle l'identité de l'appli.
 */
public class LoginFrame extends JFrame {

    private final AuthController authController = new AuthController();

    // Champs du formulaire, gardés en attributs pour les relire au clic
    private final JTextField loginField = new JTextField(18);
    private final JPasswordField mdpField = new JPasswordField(18);
    // Libellé sous le bouton, utilisé pour afficher les messages d'erreur
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        setTitle("Médiathèque de Bourg-la-Reine — Connexion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(520, 600);
        setLocationRelativeTo(null); // centre la fenêtre à l'écran
        setLayout(new BorderLayout());

        getContentPane().setBackground(UITheme.BACKGROUND);

        // Le fond est beige, on insère au centre une « carte » blanche
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(UITheme.BACKGROUND);
        centre.add(creerCarte());
        add(centre, BorderLayout.CENTER);
    }

    /** Carte blanche centrale contenant tout le formulaire de connexion. */
    private JPanel creerCarte() {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(UITheme.SURFACE);
        carte.setPreferredSize(new Dimension(420, 480));
        carte.setBorder(new CompoundBorder(
                new UITheme.RoundedBorder(UITheme.BORDER_LIGHT, 14, 1),
                new EmptyBorder(0, 0, 0, 0)));

        carte.add(creerEntete(), BorderLayout.NORTH);
        carte.add(creerFormulaire(), BorderLayout.CENTER);
        return carte;
    }

    /** Entête de la carte : logo « M » + nom de l'appli + sous-titre. */
    private JPanel creerEntete() {
        JPanel entete = new JPanel();
        entete.setOpaque(false);
        entete.setLayout(new BoxLayout(entete, BoxLayout.Y_AXIS));
        entete.setBorder(new EmptyBorder(36, 0, 8, 0));

        Logo logo = new Logo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        entete.add(logo);

        entete.add(Box.createVerticalStrut(16));

        JLabel titre = new JLabel("Médiathèque", SwingConstants.CENTER);
        titre.setFont(UITheme.DISPLAY);
        titre.setForeground(UITheme.TEXT);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        entete.add(titre);

        JLabel sous = new JLabel("Bourg-la-Reine — Espace de gestion",
                SwingConstants.CENTER);
        sous.setFont(UITheme.SMALL);
        sous.setForeground(UITheme.TEXT_MUTED);
        sous.setAlignmentX(Component.CENTER_ALIGNMENT);
        sous.setBorder(new EmptyBorder(4, 0, 0, 0));
        entete.add(sous);

        return entete;
    }

    /** Formulaire de connexion (identifiant + mot de passe + bouton). */
    private JPanel creerFormulaire() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(28, 44, 32, 44));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.weightx = 1;

        g.gridy = 0;
        JLabel lLogin = new JLabel("Identifiant");
        lLogin.setFont(UITheme.H3);
        lLogin.setForeground(UITheme.TEXT);
        p.add(lLogin, g);

        g.gridy = 1;
        g.insets = new Insets(4, 0, 12, 0);
        UITheme.styleTextField(loginField);
        p.add(loginField, g);

        g.gridy = 2;
        g.insets = new Insets(4, 0, 4, 0);
        JLabel lMdp = new JLabel("Mot de passe");
        lMdp.setFont(UITheme.H3);
        lMdp.setForeground(UITheme.TEXT);
        p.add(lMdp, g);

        g.gridy = 3;
        g.insets = new Insets(4, 0, 4, 0);
        mdpField.setFont(UITheme.NORMAL);
        mdpField.setBorder(new CompoundBorder(
                new UITheme.RoundedBorder(UITheme.BORDER, 7, 1),
                new EmptyBorder(7, 11, 7, 11)));
        // focus visuel sur le champ mot de passe : on n'utilise pas
        // UITheme.styleTextField parce que ça ne marche pas avec JPasswordField
        mdpField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                mdpField.setBorder(new CompoundBorder(
                        new UITheme.RoundedBorder(UITheme.PRIMARY, 7, 2),
                        new EmptyBorder(6, 10, 6, 10)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                mdpField.setBorder(new CompoundBorder(
                        new UITheme.RoundedBorder(UITheme.BORDER, 7, 1),
                        new EmptyBorder(7, 11, 7, 11)));
            }
        });
        p.add(mdpField, g);

        g.gridy = 4;
        g.insets = new Insets(20, 0, 6, 0);
        JButton btn = new JButton("Se connecter");
        UITheme.stylePrimaryButton(btn);
        btn.setBorder(new EmptyBorder(12, 18, 12, 18));
        p.add(btn, g);

        g.gridy = 5;
        g.insets = new Insets(8, 0, 0, 0);
        messageLabel.setForeground(UITheme.DANGER);
        messageLabel.setFont(UITheme.SMALL);
        p.add(messageLabel, g);

        // Comptes fournis avec le jeu de données de démonstration
        g.gridy = 6;
        g.insets = new Insets(20, 0, 0, 0);
        JPanel aide = new JPanel();
        aide.setLayout(new BoxLayout(aide, BoxLayout.Y_AXIS));
        aide.setBackground(UITheme.PRIMARY_CLAIR);
        aide.setBorder(new CompoundBorder(
                new UITheme.RoundedBorder(UITheme.PRIMARY_CLAIR, 8, 1),
                new EmptyBorder(10, 14, 10, 14)));
        JLabel titreAide = new JLabel("Comptes de démonstration");
        titreAide.setFont(UITheme.SMALL_BOLD);
        titreAide.setForeground(UITheme.PRIMARY_FONCE);
        titreAide.setAlignmentX(Component.LEFT_ALIGNMENT);
        aide.add(titreAide);
        JLabel ligne1 = new JLabel("admin / admin123 — administrateur");
        ligne1.setFont(UITheme.SMALL);
        ligne1.setForeground(UITheme.PRIMARY_FONCE);
        ligne1.setAlignmentX(Component.LEFT_ALIGNMENT);
        ligne1.setBorder(new EmptyBorder(3, 0, 0, 0));
        aide.add(ligne1);
        JLabel ligne2 = new JLabel("agent / agent123 — agent");
        ligne2.setFont(UITheme.SMALL);
        ligne2.setForeground(UITheme.PRIMARY_FONCE);
        ligne2.setAlignmentX(Component.LEFT_ALIGNMENT);
        aide.add(ligne2);
        p.add(aide, g);

        // Le clic sur le bouton OU la touche Entrée déclenchent la connexion
        btn.addActionListener(e -> tenterConnexion());
        mdpField.addActionListener(e -> tenterConnexion());
        loginField.addActionListener(e -> mdpField.requestFocusInWindow());
        getRootPane().setDefaultButton(btn);

        return p;
    }

    /**
     * Vérifie les identifiants saisis. Si la connexion réussit, on ouvre
     * la fenêtre principale ; sinon on affiche un message d'erreur
     * (volontairement vague pour ne pas indiquer si le login existe).
     */
    private void tenterConnexion() {
        String login = loginField.getText();
        String mdp = new String(mdpField.getPassword());
        try {
            Profil profil = authController.connecter(login, mdp);
            if (profil == null) {
                messageLabel.setText("Identifiant ou mot de passe incorrect.");
                return;
            }
            dispose(); // ferme l'écran de connexion
            new MainFrame().setVisible(true);
        } catch (RuntimeException ex) {
            // Problème technique (MySQL arrêté par exemple) : on ne montre
            // pas le détail à l'écran, on le garde dans la console.
            System.err.println("[Mediatheque] Connexion BD : " + ex.getMessage());
            messageLabel.setText("Connexion à la base impossible. Contactez l'administrateur.");
        }
    }

    /** Logo « M » dans un carré vert arrondi (centré). */
    private static final class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(72, 72));
            setMaximumSize(new Dimension(72, 72));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Carré dégradé subtil (vert -> vert plus foncé)
            g2.setColor(UITheme.PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(new Color(UITheme.PRIMARY_FONCE.getRed(),
                    UITheme.PRIMARY_FONCE.getGreen(),
                    UITheme.PRIMARY_FONCE.getBlue(), 90));
            g2.fillRoundRect(0, getHeight() / 2, getWidth(),
                    getHeight() / 2, 16, 16);
            g2.setColor(UITheme.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
            String s = "M";
            int sw = g2.getFontMetrics().stringWidth(s);
            int sh = g2.getFontMetrics().getAscent();
            g2.drawString(s, (getWidth() - sw) / 2,
                    (getHeight() + sh) / 2 - 6);
            g2.dispose();
        }
    }
}
