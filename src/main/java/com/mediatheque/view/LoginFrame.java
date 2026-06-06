package com.mediatheque.view;

import com.mediatheque.controller.AuthController;
import com.mediatheque.model.Profil;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
        setTitle("Médiathèque de Bourg-la-Reine - Connexion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(820, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(creerBanniere(), BorderLayout.WEST);
        add(creerFormulaire(), BorderLayout.CENTER);
    }

    private JPanel creerBanniere() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.PRIMARY);
        p.setPreferredSize(new Dimension(340, 0));

        JLabel titre = new JLabel("<html><div style='text-align:center;'>"
                + "MÉDIATHÈQUE<br>de Bourg-la-Reine</div></html>", SwingConstants.CENTER);
        titre.setForeground(UITheme.WHITE);
        titre.setFont(UITheme.TITLE);
        p.add(titre);
        return p;
    }

    private JPanel creerFormulaire() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;

        JLabel bienvenue = UITheme.title("Connexion agent");
        p.add(bienvenue, g);

        g.gridwidth = 1;
        g.gridy++;
        p.add(new JLabel("Identifiant"), g);
        g.gridx = 1;
        p.add(loginField, g);

        g.gridx = 0;
        g.gridy++;
        p.add(new JLabel("Mot de passe"), g);
        g.gridx = 1;
        p.add(mdpField, g);

        g.gridx = 0;
        g.gridy++;
        g.gridwidth = 2;
        JButton btn = new JButton("Se connecter");
        UITheme.stylePrimaryButton(btn);
        btn.setPreferredSize(new Dimension(0, 40));
        p.add(btn, g);

        g.gridy++;
        messageLabel.setForeground(UITheme.DANGER);
        p.add(messageLabel, g);

        g.gridy++;
        JLabel aide = new JLabel("<html><i>Comptes de test : admin/admin123 — agent/agent123</i></html>",
                SwingConstants.CENTER);
        aide.setForeground(new Color(0x888888));
        p.add(aide, g);

        btn.addActionListener(e -> tenterConnexion());
        mdpField.addActionListener(e -> tenterConnexion());
        getRootPane().setDefaultButton(btn);
        return p;
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
            // On journalise les détails techniques, et on n'affiche qu'un
            // message neutre (pas de fuite de chaîne JDBC à l'écran).
            System.err.println("[Mediatheque] Connexion BD : " + ex.getMessage());
            messageLabel.setText("Connexion à la base impossible. Contactez l'administrateur.");
        }
    }
}
