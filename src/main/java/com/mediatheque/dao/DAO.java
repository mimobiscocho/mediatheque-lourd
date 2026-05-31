package com.mediatheque.dao;

import java.util.List;

/**
 * Interface générique CRUD pour la couche d'accès aux données.
 *
 * @param <T> type du modèle géré
 */
public interface DAO<T> {

    /** Crée un nouvel enregistrement et renseigne son identifiant généré. */
    void create(T objet);

    /** Met à jour un enregistrement existant. */
    void update(T objet);

    /** Supprime l'enregistrement portant l'identifiant donné. */
    void delete(int id);

    /** Retourne l'enregistrement correspondant à l'identifiant, ou {@code null}. */
    T findById(int id);

    /** Retourne la liste de tous les enregistrements. */
    List<T> findAll();
}
