package com.mediatheque.controller;

import com.mediatheque.dao.DAOException;
import com.mediatheque.dao.TechnicienDAO;
import com.mediatheque.model.Technicien;
import java.util.List;

/**
 * Contrôleur de gestion des techniciens / animateurs.
 */
public class TechnicienController {

    private final TechnicienDAO dao = new TechnicienDAO();

    public List<Technicien> lister() {
        return dao.findAll();
    }

    public void enregistrer(Technicien t) {
        if (t.getNom() == null || t.getNom().isBlank()
                || t.getPrenom() == null || t.getPrenom().isBlank()) {
            throw new DAOException("Le nom et le prénom sont obligatoires.");
        }
        if (t.getId() > 0) {
            dao.update(t);
        } else {
            dao.create(t);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
