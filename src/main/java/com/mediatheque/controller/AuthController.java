package com.mediatheque.controller;

import com.mediatheque.dao.ProfilDAO;
import com.mediatheque.model.Profil;
import com.mediatheque.util.PasswordUtil;

/**
 * Contrôleur d'authentification : vérifie les identifiants et ouvre la session.
 */
public class AuthController {

    private final ProfilDAO profilDAO = new ProfilDAO();

    /**
     * Tente de connecter l'utilisateur.
     *
     * @return le profil connecté, ou {@code null} si échec.
     */
    public Profil connecter(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            return null;
        }
        String hash = PasswordUtil.hash(motDePasse);
        Profil profil = profilDAO.authentifier(login.trim(), hash);
        if (profil != null) {
            Session.ouvrir(profil);
        }
        return profil;
    }

    public void deconnecter() {
        Session.fermer();
    }
}
