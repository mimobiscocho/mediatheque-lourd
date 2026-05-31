package com.mediatheque.controller;

import com.mediatheque.dao.ClientDAO;
import com.mediatheque.dao.DAOException;
import com.mediatheque.model.Client;
import java.util.List;

/**
 * Contrôleur de gestion des clients (adhérents).
 */
public class ClientController {

    private final ClientDAO dao = new ClientDAO();

    public List<Client> lister() {
        return dao.findAll();
    }

    public List<Client> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) {
            return dao.findAll();
        }
        return dao.rechercher(motCle.trim());
    }

    /** Crée ou met à jour selon que l'identifiant est renseigné. */
    public void enregistrer(Client c) {
        valider(c);
        if (c.getId() > 0) {
            dao.update(c);
        } else {
            dao.create(c);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }

    private void valider(Client c) {
        if (c.getNom() == null || c.getNom().isBlank()
                || c.getPrenom() == null || c.getPrenom().isBlank()) {
            throw new DAOException("Le nom et le prénom sont obligatoires.");
        }
        if (c.getEmail() != null && !c.getEmail().isBlank()
                && !c.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new DAOException("L'adresse email n'est pas valide.");
        }
    }
}
