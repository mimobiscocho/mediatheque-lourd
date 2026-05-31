package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Technicien;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des techniciens / animateurs.
 */
public class TechnicienDAO implements DAO<Technicien> {

    private Technicien mapper(ResultSet rs) throws SQLException {
        Technicien t = new Technicien();
        t.setId(rs.getInt("id"));
        t.setNom(rs.getString("nom"));
        t.setPrenom(rs.getString("prenom"));
        t.setEmail(rs.getString("email"));
        t.setTelephone(rs.getString("telephone"));
        t.setSpecialite(rs.getString("specialite"));
        return t;
    }

    @Override
    public void create(Technicien t) {
        String sql = "INSERT INTO technicien (nom, prenom, email, telephone, specialite) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, t);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création du technicien : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Technicien t) {
        String sql = "UPDATE technicien SET nom=?, prenom=?, email=?, telephone=?, specialite=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            remplir(ps, t);
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour du technicien : " + e.getMessage(), e);
        }
    }

    private void remplir(PreparedStatement ps, Technicien t) throws SQLException {
        ps.setString(1, t.getNom());
        ps.setString(2, t.getPrenom());
        ps.setString(3, t.getEmail());
        ps.setString(4, t.getTelephone());
        ps.setString(5, t.getSpecialite());
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM technicien WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Suppression impossible : ce technicien est lié à des animations.", e);
        }
    }

    @Override
    public Technicien findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM technicien WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture du technicien.", e);
        }
        return null;
    }

    @Override
    public List<Technicien> findAll() {
        List<Technicien> liste = new ArrayList<>();
        String sql = "SELECT * FROM technicien ORDER BY nom, prenom";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des techniciens.", e);
        }
        return liste;
    }
}
