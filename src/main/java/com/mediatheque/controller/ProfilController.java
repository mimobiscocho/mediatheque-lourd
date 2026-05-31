package com.mediatheque.controller;

import com.mediatheque.dao.DAOException;
import com.mediatheque.dao.ProfilDAO;
import com.mediatheque.model.Profil;
import com.mediatheque.util.PasswordUtil;
import java.util.List;

/**
 * Contrôleur de gestion des profils (agents).
 */
public class ProfilController {

    private final ProfilDAO dao = new ProfilDAO();

    public List<Profil> lister() {
        return dao.findAll();
    }

    /**
     * Enregistre un profil. Le mot de passe en clair (s'il est fourni) est
     * haché avant persistance ; laissé vide en modification, il reste inchangé.
     */
    public void enregistrer(Profil p, String motDePasseClair) {
        if (p.getLogin() == null || p.getLogin().isBlank()) {
            throw new DAOException("Le login est obligatoire.");
        }
        if (p.getNom() == null || p.getNom().isBlank()
                || p.getPrenom() == null || p.getPrenom().isBlank()) {
            throw new DAOException("Le nom et le prénom sont obligatoires.");
        }
        if (motDePasseClair != null && !motDePasseClair.isBlank()) {
            p.setMotDePasse(PasswordUtil.hash(motDePasseClair));
        } else if (p.getId() == 0) {
            throw new DAOException("Le mot de passe est obligatoire pour un nouveau profil.");
        } else {
            p.setMotDePasse(null); // signal au DAO de ne pas modifier le mot de passe
        }

        if (p.getId() > 0) {
            dao.update(p);
        } else {
            dao.create(p);
        }
    }

    public void supprimer(int id) {
        if (Session.getProfil() != null && Session.getProfil().getId() == id) {
            throw new DAOException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        dao.delete(id);
    }
}
