package com.mediatheque.controller;

import com.mediatheque.model.Profil;

/**
 * Conserve le profil connecté durant la session applicative.
 */
public final class Session {

    private static Profil profilConnecte;

    private Session() {
    }

    public static void ouvrir(Profil profil) {
        profilConnecte = profil;
    }

    public static Profil getProfil() {
        return profilConnecte;
    }

    public static boolean estAdmin() {
        return profilConnecte != null && profilConnecte.estAdmin();
    }

    public static void fermer() {
        profilConnecte = null;
    }
}
