package com.mediatheque;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Point d'entrée de l'application de gestion de la médiathèque (Client Lourd).
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Look and feel par défaut en cas d'échec.
        }

        // Ferme proprement la connexion à la fermeture de la JVM.
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::close));

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
