package com.mediatheque.dao;

import com.mediatheque.config.DatabaseConnection;
import com.mediatheque.model.Profil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des profils (agents) et l'authentification.
 */
public class ProfilDAO implements DAO<Profil> {

    private Profil mapper(ResultSet rs) throws SQLException {
        Profil p = new Profil();
        p.setId(rs.getInt("id"));
        p.setLogin(rs.getString("login"));
        p.setMotDePasse(rs.getString("mot_de_passe"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setRole(rs.getString("role"));
        return p;
    }

    /**
     * Recherche un profil par login, puis vérifie le mot de passe en mémoire
     * (PBKDF2 salé : on ne peut pas comparer le hash directement en SQL).
     *
     * @return le profil si les identifiants sont valides, sinon {@code null}.
     */
    public Profil authentifier(String login, String motDePasseClair) {
        String sql = "SELECT * FROM profil WHERE login = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Profil p = mapper(rs);
                    if (com.mediatheque.util.PasswordUtil.verify(motDePasseClair, p.getMotDePasse())) {
                        return p;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de l'authentification.", e);
        }
        return null;
    }

    @Override
    public void create(Profil p) {
        String sql = "INSERT INTO profil (login, mot_de_passe, nom, prenom, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getLogin());
            ps.setString(2, p.getMotDePasse());
            ps.setString(3, p.getNom());
            ps.setString(4, p.getPrenom());
            ps.setString(5, p.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création du profil : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Profil p) {
        boolean changeMdp = p.getMotDePasse() != null && !p.getMotDePasse().isBlank();
        String sql = changeMdp
                ? "UPDATE profil SET login=?, mot_de_passe=?, nom=?, prenom=?, role=? WHERE id=?"
                : "UPDATE profil SET login=?, nom=?, prenom=?, role=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, p.getLogin());
            if (changeMdp) {
                ps.setString(i++, p.getMotDePasse());
            }
            ps.setString(i++, p.getNom());
            ps.setString(i++, p.getPrenom());
            ps.setString(i++, p.getRole());
            ps.setInt(i, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour du profil : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM profil WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression du profil : " + e.getMessage(), e);
        }
    }

    @Override
    public Profil findById(int id) {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM profil WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture du profil.", e);
        }
        return null;
    }

    @Override
    public List<Profil> findAll() {
        List<Profil> liste = new ArrayList<>();
        String sql = "SELECT * FROM profil ORDER BY nom, prenom";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des profils.", e);
        }
        return liste;
    }
}
