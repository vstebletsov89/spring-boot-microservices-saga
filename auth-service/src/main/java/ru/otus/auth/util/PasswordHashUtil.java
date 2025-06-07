package ru.otus.auth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHashUtil {

    public static byte[] hashPassword(String password, String algorithm) {
        try {
            var md = MessageDigest.getInstance(algorithm); // "MD5", "SHA-256", "SHA-512"
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unsupported hash algorithm: " + algorithm, e);
        }
    }
}
