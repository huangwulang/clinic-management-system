package com.guet.clinic.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    private static final String MD5_PATTERN = "^[a-fA-F0-9]{32}$";

    public static String encode(String rawPassword) {
        return digest("SHA-256", rawPassword);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return rawPassword == null;
        }
        return storedPassword.equals(rawPassword) || storedPassword.equals(encode(rawPassword));
    }

    public static String md5(String rawPassword) {
        return digest("MD5", rawPassword);
    }

    public static String encodeClientPassword(String password) {
        if (password == null) {
            return null;
        }
        return encode(normalizeClientPassword(password));
    }

    public static boolean matchesClientPassword(String password, String storedPassword) {
        if (storedPassword == null) {
            return password == null;
        }
        return storedPassword.equals(normalizeClientPassword(password))
                || storedPassword.equals(encodeClientPassword(password))
                || matches(password, storedPassword);
    }

    private static String normalizeClientPassword(String password) {
        if (password != null && password.matches(MD5_PATTERN)) {
            return password.toLowerCase();
        }
        return md5(password);
    }

    private static String digest(String algorithm, String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : encoded) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(algorithm + " is not available", exception);
        }
    }

    private PasswordUtil() {
    }
}
