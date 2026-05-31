package com.mediatheque.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gère la connexion unique (singleton) à la base de données MySQL via JDBC.
 * Les paramètres sont lus dans le fichier {@code database.properties}.
 */
public final class DatabaseConnection {

    private static Connection connection;
    private static String url;
    private static String user;
    private static String password;

    private DatabaseConnection() {
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class
                .getClassLoader().getResourceAsStream("database.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "Fichier database.properties introuvable dans le classpath.");
            }
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Impossible de charger la configuration de la base : " + e.getMessage(), e);
        }
    }

    /**
     * Retourne la connexion active, en l'ouvrant si nécessaire.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (url == null) {
                loadProperties();
            }
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Pilote JDBC MySQL introuvable.", e);
            }
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    /**
     * Ferme proprement la connexion (à appeler à la fermeture de l'application).
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}
