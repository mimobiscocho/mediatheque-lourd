package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DAO dédié aux compteurs du tableau de bord.
 * Réalise une seule requête {@code SELECT COUNT(*)} agrégée par table,
 * au lieu de charger l'intégralité de chaque table juste pour {@code size()}.
 */
public class StatsDAO {

    /**
     * Retourne un dictionnaire ordonné des nombres d'enregistrements par table.
     * Clés : libellés affichables ; valeurs : compteurs.
     */
    public Map<String, Integer> compteurs() {
        String sql =
                "SELECT (SELECT COUNT(*) FROM client)      AS nb_clients,"
              + "       (SELECT COUNT(*) FROM technicien)  AS nb_techniciens,"
              + "       (SELECT COUNT(*) FROM salle)       AS nb_salles,"
              + "       (SELECT COUNT(*) FROM animation)   AS nb_animations,"
              + "       (SELECT COUNT(*) FROM reservation) AS nb_reservations,"
              + "       (SELECT COUNT(*) FROM facture)     AS nb_factures";
        Map<String, Integer> resultat = new LinkedHashMap<>();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                resultat.put("Clients",      rs.getInt("nb_clients"));
                resultat.put("Techniciens",  rs.getInt("nb_techniciens"));
                resultat.put("Salles",       rs.getInt("nb_salles"));
                resultat.put("Animations",   rs.getInt("nb_animations"));
                resultat.put("Réservations", rs.getInt("nb_reservations"));
                resultat.put("Factures",     rs.getInt("nb_factures"));
            }
        } catch (SQLException e) {
            throw new DAOException("Impossible de calculer les indicateurs du tableau de bord.", e);
        }
        return resultat;
    }
}
