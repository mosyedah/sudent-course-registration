package enrollment.courseenrollment.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InputUtils {

    // Validate email format
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    // Validate name (non-empty, letters only, min 2 chars)
    public static boolean isValidName(String name) {
        if (name == null) return false;
        return name.matches("^[A-Za-z ]{2,50}$");
    }

    // Validate password (non-empty, min 6 chars)
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= 6;
    }

    // Hash password using SHA-256
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available");
        }
    }
}
