package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Salle;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des salles de coworking.
 */
public class SalleDAO implements DAO<Salle> {

    private Salle mapper(ResultSet rs) throws SQLException {
        Salle s = new Salle();
        s.setId(rs.getInt("id"));
        s.setNom(rs.getString("nom"));
        s.setCapacite(rs.getInt("capacite"));
        s.setEquipement(rs.getString("equipement"));
        s.setDisponible(rs.getBoolean("disponible"));
        return s;
    }

    @Override
    public void create(Salle s) {
        String sql = "INSERT INTO salle (nom, capacite, equipement, disponible) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, s);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création de la salle : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Salle s) {
        String sql = "UPDATE salle SET nom=?, capacite=?, equipement=?, disponible=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            remplir(ps, s);
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de la salle : " + e.getMessage(), e);
        }
    }

    private void remplir(PreparedStatement ps, Salle s) throws SQLException {
        ps.setString(1, s.getNom());
        ps.setInt(2, s.getCapacite());
        ps.setString(3, s.getEquipement());
        ps.setBoolean(4, s.isDisponible());
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM salle WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Suppression impossible : cette salle est utilisée (animations / réservations).", e);
        }
    }

    @Override
    public Salle findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM salle WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture de la salle.", e);
        }
        return null;
    }

    @Override
    public List<Salle> findAll() {
        List<Salle> liste = new ArrayList<>();
        String sql = "SELECT * FROM salle ORDER BY nom";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des salles.", e);
        }
        return liste;
    }
}
