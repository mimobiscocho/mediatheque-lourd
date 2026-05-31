package com.mediatheque.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaire de hachage des mots de passe (SHA-256).
 * Les empreintes stockées en base correspondent à ce hachage.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Retourne l'empreinte SHA-256 (hexadécimal) d'un mot de passe en clair.
     */
    public static String hash(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible.", e);
        }
    }
}
