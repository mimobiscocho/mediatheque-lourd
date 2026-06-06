package com.mediatheque.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitaire de hachage des mots de passe avec PBKDF2-HMAC-SHA256.
 *
 * <p>Un sel aléatoire de 16 octets est généré à chaque hachage, et 600 000
 * itérations sont appliquées (recommandation OWASP 2023+). Les empreintes
 * sont stockées au format :
 * <pre>pbkdf2_sha256${iterations}${saltBase64}${hashBase64}</pre>
 * Le sel est inclus dans la chaîne stockée : la vérification est donc
 * autonome (pas de table de sels séparée).
 */
public final class PasswordUtil {

    private static final String ALGO       = "PBKDF2WithHmacSHA256";
    private static final int    ITERATIONS = 600_000;
    private static final int    SALT_BYTES = 16;
    private static final int    KEY_BYTES  = 32;
    private static final String PREFIX     = "pbkdf2_sha256";

    private PasswordUtil() {
    }

    /**
     * Calcule une empreinte salée du mot de passe en clair.
     */
    public static String hash(String motDePasse) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(motDePasse.toCharArray(), salt, ITERATIONS, KEY_BYTES);
        return PREFIX + "$" + ITERATIONS + "$"
             + Base64.getEncoder().encodeToString(salt) + "$"
             + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Vérifie qu'un mot de passe en clair correspond à une empreinte stockée.
     * Comparaison à temps constant pour éviter les attaques par timing.
     */
    public static boolean verify(String motDePasse, String empreinte) {
        if (motDePasse == null || empreinte == null) {
            return false;
        }
        String[] parts = empreinte.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int    iter     = Integer.parseInt(parts[1]);
            byte[] salt     = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual   = pbkdf2(motDePasse.toCharArray(), salt, iter, expected.length);
            return constantTimeEquals(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLen) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLen * 8);
            return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Hachage PBKDF2 indisponible.", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
