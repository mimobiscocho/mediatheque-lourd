package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Facture;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des factures (jointure client).
 */
public class FactureDAO implements DAO<Facture> {

    private static final String SELECT_BASE =
            "SELECT f.*, CONCAT(c.prenom, ' ', c.nom) AS client_nom "
          + "FROM facture f JOIN client c ON f.client_id = c.id ";

    private Facture mapper(ResultSet rs) throws SQLException {
        Facture f = new Facture();
        f.setId(rs.getInt("id"));
        f.setClientId(rs.getInt("client_id"));
        f.setLibelle(rs.getString("libelle"));
        f.setMontant(rs.getBigDecimal("montant"));
        f.setDateEmission(rs.getDate("date_emission").toLocalDate());
        f.setStatut(rs.getString("statut"));
        f.setClientNom(rs.getString("client_nom"));
        return f;
    }

    @Override
    public void create(Facture f) {
        String sql = "INSERT INTO facture (client_id, libelle, montant, date_emission, statut) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, f);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    f.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création de la facture : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Facture f) {
        String sql = "UPDATE facture SET client_id=?, libelle=?, montant=?, date_emission=?, statut=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            remplir(ps, f);
            ps.setInt(6, f.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de la facture : " + e.getMessage(), e);
        }
    }

    private void remplir(PreparedStatement ps, Facture f) throws SQLException {
        ps.setInt(1, f.getClientId());
        ps.setString(2, f.getLibelle());
        ps.setBigDecimal(3, f.getMontant());
        ps.setDate(4, Date.valueOf(f.getDateEmission()));
        ps.setString(5, f.getStatut());
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM facture WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression de la facture : " + e.getMessage(), e);
        }
    }

    @Override
    public Facture findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(SELECT_BASE + "WHERE f.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture de la facture.", e);
        }
        return null;
    }

    @Override
    public List<Facture> findAll() {
        List<Facture> liste = new ArrayList<>();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY f.date_emission DESC")) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des factures.", e);
        }
        return liste;
    }
}
