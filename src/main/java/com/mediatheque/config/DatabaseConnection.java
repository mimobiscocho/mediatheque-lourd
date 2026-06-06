package com.mediatheque.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gère la connexion unique (singleton) à la base de données MySQL via JDBC.
 *
 * <p>Les paramètres sont lus dans cet ordre de priorité :
 * <ol>
 *   <li>Variables d'environnement {@code MEDIATHEQUE_DB_URL},
 *       {@code MEDIATHEQUE_DB_USER}, {@code MEDIATHEQUE_DB_PASSWORD}
 *       — utilisées en production pour ne pas versionner les secrets ;</li>
 *   <li>Fichier {@code database.properties} sur le classpath, sinon.</li>
 * </ol>
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
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Impossible de charger la configuration de la base : " + e.getMessage(), e);
        }
        url      = firstNonBlank(System.getenv("MEDIATHEQUE_DB_URL"),      props.getProperty("db.url"));
        user     = firstNonBlank(System.getenv("MEDIATHEQUE_DB_USER"),     props.getProperty("db.user"));
        password = firstNonBlank(System.getenv("MEDIATHEQUE_DB_PASSWORD"), props.getProperty("db.password"));

        if (url == null) {
            throw new IllegalStateException(
                    "URL JDBC non configurée (MEDIATHEQUE_DB_URL ou database.properties).");
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
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
