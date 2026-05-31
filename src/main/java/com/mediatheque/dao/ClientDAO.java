package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Client;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des clients (adhérents).
 */
public class ClientDAO implements DAO<Client> {

    private Client mapper(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setEmail(rs.getString("email"));
        c.setTelephone(rs.getString("telephone"));
        c.setAdresse(rs.getString("adresse"));
        c.setTypeAbonnement(rs.getString("type_abonnement"));
        Date d = rs.getDate("date_inscription");
        if (d != null) {
            c.setDateInscription(d.toLocalDate());
        }
        c.setActif(rs.getBoolean("actif"));
        return c;
    }

    @Override
    public void create(Client c) {
        String sql = "INSERT INTO client (nom, prenom, email, telephone, adresse, type_abonnement, date_inscription, actif) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, c);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création du client : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Client c) {
        String sql = "UPDATE client SET nom=?, prenom=?, email=?, telephone=?, adresse=?, type_abonnement=?, date_inscription=?, actif=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            remplir(ps, c);
            ps.setInt(9, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour du client : " + e.getMessage(), e);
        }
    }

    private void remplir(PreparedStatement ps, Client c) throws SQLException {
        ps.setString(1, c.getNom());
        ps.setString(2, c.getPrenom());
        ps.setString(3, c.getEmail());
        ps.setString(4, c.getTelephone());
        ps.setString(5, c.getAdresse());
        ps.setString(6, c.getTypeAbonnement());
        ps.setDate(7, Date.valueOf(c.getDateInscription()));
        ps.setBoolean(8, c.isActif());
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM client WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression du client : " + e.getMessage(), e);
        }
    }

    @Override
    public Client findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM client WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture du client.", e);
        }
        return null;
    }

    @Override
    public List<Client> findAll() {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom, prenom";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des clients.", e);
        }
        return liste;
    }

    /** Recherche multicritères sur nom, prénom ou email. */
    public List<Client> rechercher(String motCle) {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? ORDER BY nom, prenom";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            String like = "%" + motCle + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche de clients.", e);
        }
        return liste;
    }
}
