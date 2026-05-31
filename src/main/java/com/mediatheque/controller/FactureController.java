package com.mediatheque.controller;

import com.mediatheque.dao.DAOException;
import com.mediatheque.dao.FactureDAO;
import com.mediatheque.model.Facture;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contrôleur de gestion des factures.
 */
public class FactureController {

    private final FactureDAO dao = new FactureDAO();

    public List<Facture> lister() {
        return dao.findAll();
    }

    public void enregistrer(Facture f) {
        if (f.getClientId() <= 0) {
            throw new DAOException("Un client doit être sélectionné.");
        }
        if (f.getLibelle() == null || f.getLibelle().isBlank()) {
            throw new DAOException("Le libellé est obligatoire.");
        }
        if (f.getMontant() == null || f.getMontant().compareTo(BigDecimal.ZERO) < 0) {
            throw new DAOException("Le montant doit être positif.");
        }
        if (f.getId() > 0) {
            dao.update(f);
        } else {
            dao.create(f);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
