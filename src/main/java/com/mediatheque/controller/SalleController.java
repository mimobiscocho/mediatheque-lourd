package com.mediatheque.controller;

import com.mediatheque.dao.DAOException;
import com.mediatheque.dao.SalleDAO;
import com.mediatheque.model.Salle;
import java.util.List;

/**
 * Contrôleur de gestion des salles de coworking.
 */
public class SalleController {

    private final SalleDAO dao = new SalleDAO();

    public List<Salle> lister() {
        return dao.findAll();
    }

    public void enregistrer(Salle s) {
        if (s.getNom() == null || s.getNom().isBlank()) {
            throw new DAOException("Le nom de la salle est obligatoire.");
        }
        if (s.getCapacite() <= 0) {
            throw new DAOException("La capacité doit être strictement positive.");
        }
        if (s.getId() > 0) {
            dao.update(s);
        } else {
            dao.create(s);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
