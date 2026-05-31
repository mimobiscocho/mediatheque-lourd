package com.mediatheque.dao;

/**
 * Exception non vérifiée encapsulant les erreurs de la couche d'accès
 * aux données, afin de remonter un message lisible aux vues.
 */
public class DAOException extends RuntimeException {

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(String message) {
        super(message);
    }
}
