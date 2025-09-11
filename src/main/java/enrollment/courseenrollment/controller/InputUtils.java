package enrollment.courseenrollment.controller;


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

    
}
