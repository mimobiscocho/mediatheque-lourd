package com.mediatheque.controller;

import com.mediatheque.dao.DAOException;
import com.mediatheque.dao.ReservationDAO;
import com.mediatheque.model.Reservation;
import java.util.List;

/**
 * Contrôleur de gestion des réservations de salles.
 */
public class ReservationController {

    private final ReservationDAO dao = new ReservationDAO();

    public List<Reservation> lister() {
        return dao.findAll();
    }

    public void enregistrer(Reservation r) {
        if (r.getClientId() <= 0 || r.getSalleId() <= 0) {
            throw new DAOException("Le client et la salle doivent être sélectionnés.");
        }
        if (r.getHeureDebut() == null || r.getHeureFin() == null
                || !r.getHeureFin().isAfter(r.getHeureDebut())) {
            throw new DAOException("L'heure de fin doit être postérieure à l'heure de début.");
        }
        if (r.getId() > 0) {
            dao.update(r);
        } else {
            dao.create(r);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
