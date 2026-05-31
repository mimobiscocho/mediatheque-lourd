package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Reservation;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des réservations de salles.
 * Les contrôles de disponibilité sont assurés par les triggers MySQL.
 */
public class ReservationDAO implements DAO<Reservation> {

    private static final String SELECT_BASE =
            "SELECT r.*, CONCAT(c.prenom, ' ', c.nom) AS client_nom, s.nom AS salle_nom "
          + "FROM reservation r "
          + "JOIN client c ON r.client_id = c.id "
          + "JOIN salle s ON r.salle_id = s.id ";

    private Reservation mapper(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setClientId(rs.getInt("client_id"));
        r.setSalleId(rs.getInt("salle_id"));
        r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
        r.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        r.setHeureFin(rs.getTime("heure_fin").toLocalTime());
        r.setStatut(rs.getString("statut"));
        r.setClientNom(rs.getString("client_nom"));
        r.setSalleNom(rs.getString("salle_nom"));
        return r;
    }

    @Override
    public void create(Reservation r) {
        String sql = "INSERT INTO reservation (client_id, salle_id, date_reservation, heure_debut, heure_fin, statut) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, r);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            // Les triggers renvoient des messages métier explicites (SQLSTATE 45000)
            throw new DAOException(messageMetier(e), e);
        }
    }

    @Override
    public void update(Reservation r) {
        String sql = "UPDATE reservation SET client_id=?, salle_id=?, date_reservation=?, heure_debut=?, heure_fin=?, statut=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            remplir(ps, r);
            ps.setInt(7, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(messageMetier(e), e);
        }
    }

    private void remplir(PreparedStatement ps, Reservation r) throws SQLException {
        ps.setInt(1, r.getClientId());
        ps.setInt(2, r.getSalleId());
        ps.setDate(3, Date.valueOf(r.getDateReservation()));
        ps.setTime(4, Time.valueOf(r.getHeureDebut()));
        ps.setTime(5, Time.valueOf(r.getHeureFin()));
        ps.setString(6, r.getStatut());
    }

    private String messageMetier(SQLException e) {
        // SQLSTATE 45000 = erreur levée par un trigger (SIGNAL)
        if ("45000".equals(e.getSQLState())) {
            return e.getMessage();
        }
        return "Erreur lors de l'enregistrement de la réservation : " + e.getMessage();
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM reservation WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression de la réservation : " + e.getMessage(), e);
        }
    }

    @Override
    public Reservation findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(SELECT_BASE + "WHERE r.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture de la réservation.", e);
        }
        return null;
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> liste = new ArrayList<>();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY r.date_reservation DESC, r.heure_debut")) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des réservations.", e);
        }
        return liste;
    }
}
