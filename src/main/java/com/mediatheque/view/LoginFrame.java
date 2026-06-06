package com.mediatheque.view;

import com.mediatheque.controller.AuthController;
import com.mediatheque.model.Profil;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Fenêtre de connexion : authentifie l'agent avant d'ouvrir l'application.
 */
public class LoginFrame extends JFrame {

    private final AuthController authController = new AuthController();
    private final JTextField loginField = new JTextField(18);
    private final JPasswordField mdpField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        setTitle("Médiathèque de Bourg-la-Reine — Connexion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(880, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.SURFACE);

        add(creerBanniere(), BorderLayout.WEST);
        add(creerFormulaire(), BorderLayout.CENTER);
    }

    private JPanel creerBanniere() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.PRIMARY);
        p.setPreferredSize(new Dimension(360, 0));
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new javax.swing.BoxLayout(inner, javax.swing.BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("MB");
        logo.setOpaque(true);
        logo.setBackground(new Color(255, 255, 255, 38));
        logo.setForeground(UITheme.WHITE);
        logo.setFont(new Font(UITheme.TITLE.getFamily(), Font.BOLD, 26));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        logo.setMaximumSize(new Dimension(70, 70));
        logo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JLabel titre = new JLabel("MÉDIATHÈQUE");
        titre.setForeground(UITheme.WHITE);
        titre.setFont(new Font(UITheme.TITLE.getFamily(), Font.BOLD, 24));
        titre.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JLabel sous = new JLabel("de Bourg-la-Reine");
        sous.setForeground(new Color(255, 255, 255, 200));
        sous.setFont(UITheme.NORMAL);
        sous.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><div style='text-align:center;'>"
                + "Application de gestion<br>réservée au personnel.</div></html>");
        desc.setForeground(new Color(255, 255, 255, 180));
        desc.setFont(UITheme.SMALL);
        desc.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        inner.add(logo);
        inner.add(javax.swing.Box.createVerticalStrut(24));
        inner.add(titre);
        inner.add(javax.swing.Box.createVerticalStrut(4));
        inner.add(sous);
        inner.add(javax.swing.Box.createVerticalStrut(28));
        inner.add(desc);

        p.add(inner);
        return p;
    }

    private JPanel creerFormulaire() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.SURFACE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.weightx = 1;

        JLabel bienvenue = new JLabel("Bon retour");
        bienvenue.setFont(UITheme.TITLE);
        bienvenue.setForeground(UITheme.TEXT);
        p.add(bienvenue, g);

        g.gridy++;
        JLabel hint = new JLabel("Connectez-vous pour accéder à votre espace.");
        hint.setFont(UITheme.NORMAL);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        p.add(hint, g);

        g.gridy++;
        p.add(fieldLabel("Identifiant"), g);
        g.gridy++;
        styleField(loginField);
        p.add(loginField, g);

        g.gridy++;
        g.insets = new Insets(14, 0, 6, 0);
        p.add(fieldLabel("Mot de passe"), g);
        g.gridy++;
        g.insets = new Insets(6, 0, 6, 0);
        styleField(mdpField);
        p.add(mdpField, g);

        g.gridy++;
        g.insets = new Insets(20, 0, 6, 0);
        JButton btn = new JButton("Se connecter");
        UITheme.stylePrimaryButton(btn);
        btn.setFont(UITheme.H3);
        btn.setPreferredSize(new Dimension(0, 42));
        p.add(btn, g);

        g.gridy++;
        g.insets = new Insets(8, 0, 0, 0);
        messageLabel.setForeground(UITheme.DANGER);
        messageLabel.setFont(UITheme.SMALL);
        p.add(messageLabel, g);

        g.gridy++;
        g.insets = new Insets(20, 0, 0, 0);
        JLabel aide = new JLabel("<html><div style='text-align:center;'>"
                + "<span style='color:#64748B;'>Comptes de démonstration :</span><br>"
                + "<b>admin</b> / admin123 &nbsp;·&nbsp; <b>agent</b> / agent123</div></html>",
                SwingConstants.CENTER);
        aide.setFont(UITheme.SMALL);
        p.add(aide, g);

        btn.addActionListener(e -> tenterConnexion());
        mdpField.addActionListener(e -> tenterConnexion());
        getRootPane().setDefaultButton(btn);

        GridBagConstraints wc = new GridBagConstraints();
        wc.fill = GridBagConstraints.BOTH;
        wc.weightx = 1;
        wc.weighty = 1;
        wrapper.add(p, wc);
        return wrapper;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.H3);
        l.setForeground(UITheme.TEXT);
        return l;
    }

    private void styleField(javax.swing.JTextField field) {
        field.setFont(UITheme.NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        field.setBackground(UITheme.SURFACE);
        field.setForeground(UITheme.TEXT);
    }

    private void tenterConnexion() {
        String login = loginField.getText();
        String mdp = new String(mdpField.getPassword());
        try {
            Profil profil = authController.connecter(login, mdp);
            if (profil == null) {
                messageLabel.setText("Identifiant ou mot de passe incorrect.");
                return;
            }
            dispose();
            new MainFrame().setVisible(true);
        } catch (RuntimeException ex) {
            System.err.println("[Mediatheque] Connexion BD : " + ex.getMessage());
            messageLabel.setText("Connexion à la base impossible. Contactez l'administrateur.");
        }
    }
}
