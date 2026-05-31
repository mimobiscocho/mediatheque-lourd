package com.mediatheque.controller;

import com.mediatheque.dao.AnimationDAO;
import com.mediatheque.dao.DAOException;
import com.mediatheque.model.Animation;
import java.util.List;

/**
 * Contrôleur de gestion des animations.
 */
public class AnimationController {

    private final AnimationDAO dao = new AnimationDAO();

    public List<Animation> lister() {
        return dao.findAll();
    }

    public void enregistrer(Animation a) {
        if (a.getTitre() == null || a.getTitre().isBlank()) {
            throw new DAOException("Le titre de l'animation est obligatoire.");
        }
        if (a.getHeureDebut() == null || a.getHeureFin() == null
                || !a.getHeureFin().isAfter(a.getHeureDebut())) {
            throw new DAOException("L'heure de fin doit être postérieure à l'heure de début.");
        }
        if (a.getSalleId() <= 0 || a.getTechnicienId() <= 0) {
            throw new DAOException("La salle et le technicien doivent être sélectionnés.");
        }
        if (a.getId() > 0) {
            dao.update(a);
        } else {
            dao.create(a);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
