package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Animation;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des animations (jointures salle + technicien).
 */
public class AnimationDAO implements DAO<Animation> {

    private static final String SELECT_BASE =
            "SELECT a.*, s.nom AS salle_nom, "
          + "CONCAT(t.prenom, ' ', t.nom) AS technicien_nom "
          + "FROM animation a "
          + "JOIN salle s ON a.salle_id = s.id "
          + "JOIN technicien t ON a.technicien_id = t.id ";

    private Animation mapper(ResultSet rs) throws SQLException {
        Animation a = new Animation();
        a.setId(rs.getInt("id"));
        a.setTitre(rs.getString("titre"));
        a.setDescription(rs.getString("description"));
        a.setDateAnimation(rs.getDate("date_animation").toLocalDate());
        a.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        a.setHeureFin(rs.getTime("heure_fin").toLocalTime());
        a.setNbPlaces(rs.getInt("nb_places"));
        a.setSalleId(rs.getInt("salle_id"));
        a.setTechnicienId(rs.getInt("technicien_id"));
        a.setSalleNom(rs.getString("salle_nom"));
        a.setTechnicienNom(rs.getString("technicien_nom"));
        return a;
    }

    @Override
    public void create(Animation a) {
        String sql = "INSERT INTO animation (titre, description, date_animation, heure_debut, heure_fin, nb_places, salle_id, technicien_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, a);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création de l'animation : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Animation a) {
        String sql = "UPDATE animation SET titre=?, description=?, date_animation=?, heure_debut=?, heure_fin=?, nb_places=?, salle_id=?, technicien_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            remplir(ps, a);
            ps.setInt(9, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de l'animation : " + e.getMessage(), e);
        }
    }

    private void remplir(PreparedStatement ps, Animation a) throws SQLException {
        ps.setString(1, a.getTitre());
        ps.setString(2, a.getDescription());
        ps.setDate(3, Date.valueOf(a.getDateAnimation()));
        ps.setTime(4, Time.valueOf(a.getHeureDebut()));
        ps.setTime(5, Time.valueOf(a.getHeureFin()));
        ps.setInt(6, a.getNbPlaces());
        ps.setInt(7, a.getSalleId());
        ps.setInt(8, a.getTechnicienId());
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM animation WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression de l'animation : " + e.getMessage(), e);
        }
    }

    @Override
    public Animation findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(SELECT_BASE + "WHERE a.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture de l'animation.", e);
        }
        return null;
    }

    @Override
    public List<Animation> findAll() {
        List<Animation> liste = new ArrayList<>();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY a.date_animation, a.heure_debut")) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des animations.", e);
        }
        return liste;
    }
}
