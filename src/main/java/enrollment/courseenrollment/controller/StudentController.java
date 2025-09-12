package enrollment.courseenrollment.controller;

import enrollment.courseenrollment.exceptions.EmailAlreadyExistsException;
import enrollment.courseenrollment.exceptions.InvalidCredentialsException;
import enrollment.courseenrollment.exceptions.StudentNotFoundException;
import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.service.StudentService;

import java.util.Scanner;

/**
 * Handles all student-related actions after signup/login.
 * Uses singleton SessionManager to check session and track current user.
 */
public class StudentController {

    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BOLD = "\u001B[1m";

    private final StudentService studentService;
    private final Scanner scanner;
    private Student loggedInStudent;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Signs up a new student and starts a session if successful.
     */
    public void signUp() {
        System.out.println("\n" + CYAN + BOLD + "--- Sign Up ---" + RESET);

        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        if (!InputUtils.isValidName(name)) {
            printMessage("Invalid name. Must be letters only, 2-50 chars.", YELLOW);
            return;
        }

        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        if (!InputUtils.isValidEmail(email)) {
            printMessage("Invalid email format.", YELLOW);
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (!InputUtils.isValidPassword(password)) {
            printMessage("Password must be at least 6 characters.", YELLOW);
            return;
        }

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPasswordHash(password);

        try {
            student = studentService.signUp(student);
            printMessage("Thank you! Your account is created and you’re now logged in.", GREEN);
            this.loggedInStudent = student;
            SessionManager.createSession(student.getStudentId());
        } catch (EmailAlreadyExistsException e) {
            printMessage("Error: " + e.getMessage(), RED);
        }
    }

    /**
     * Logs in an existing student and starts a session if successful.
     */
    public void login() {
        System.out.println("\n" + CYAN + BOLD + "--- Login ---" + RESET);

        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            Student student = studentService.login(email, password);
            if (student != null) {
                printMessage("Welcome back! You’re now logged in.", GREEN);
                this.loggedInStudent = student;
                SessionManager.createSession(student.getStudentId());
            }
        } catch (StudentNotFoundException | InvalidCredentialsException e) {
            printMessage("Error: " + e.getMessage(), RED);
        }
    }

    /**
     * Displays logged-in student's profile.
     */
    public void viewProfile() {
        if (!isSessionActive()) return;

        try {
            loggedInStudent = studentService.viewProfile(loggedInStudent.getStudentId());

            System.out.println("\n" + CYAN + BOLD + "--- Your Profile Details ---" + RESET);
            System.out.println(YELLOW + "Name:  " + RESET + loggedInStudent.getName());
            System.out.println(YELLOW + "Email: " + RESET + loggedInStudent.getEmail());
        } catch (StudentNotFoundException e) {
            printMessage("Error: " + e.getMessage(), RED);
        }
    }

    /**
     * Updates the logged-in student's profile.
     */
    public void updateProfile() {
        if (!isSessionActive()) return;

        System.out.println("\n" + CYAN + BOLD + "--- Update Profile ---" + RESET);

        System.out.print("Enter new name (or leave blank to keep current): ");
        String name = scanner.nextLine();
        if (!name.isBlank() && !InputUtils.isValidName(name)) {
            printMessage("Invalid name. Update aborted.", YELLOW);
            return;
        }

        System.out.print("Enter new email (or leave blank to keep current): ");
        String email = scanner.nextLine();
        if (!email.isBlank() && !InputUtils.isValidEmail(email)) {
            printMessage("Invalid email. Update aborted.", YELLOW);
            return;
        }

        if (name.isBlank() && email.isBlank()) {
            printMessage("No changes requested.", YELLOW);
            return;
        }

        Student student = new Student(loggedInStudent);
        student.setEmail(email.isBlank() ? loggedInStudent.getEmail() : email);
        student.setName(name.isBlank() ? loggedInStudent.getName() : name);

        boolean isEmailUpdate = !email.isBlank();

        try {
            loggedInStudent = studentService.updateProfile(student, isEmailUpdate);
            printMessage("Profile updated successfully.", GREEN);
        } catch (EmailAlreadyExistsException | StudentNotFoundException e) {
            printMessage("Error: " + e.getMessage(), RED);
        }
    }

    /**
     * Changes the password of the logged-in student.
     */
    public void changePassword() {
    	if (!isSessionActive()) return;

        System.out.println("\n" + CYAN + BOLD + "--- Change Password ---" + RESET);
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        // double check, session may expire while user types
        if (!isSessionActive()) return;

        if (!InputUtils.isValidPassword(newPassword)) {
            printMessage("Password must be at least 6 characters. Update aborted.", YELLOW);
            return;
        }

        try {
            boolean success = studentService.changePassword(
                    loggedInStudent.getStudentId(), newPassword);

            if (success) {
                printMessage("Password changed successfully.", GREEN);
            } else {
                printMessage("Password change failed.", RED);
            }
        } catch (StudentNotFoundException e) {
            printMessage("Error: " + e.getMessage(), RED);
        }
    }

    /**
     * Returns the logged-in student's name for menu greetings.
     */
    public String getLoggedInStudentName() {
        return loggedInStudent != null ? loggedInStudent.getName() : "Student";
    }

    /**
     * Checks if session is active. If not, prints a message.
     */
    private boolean isSessionActive() {
        SessionManager session = SessionManager.getInstance();
        if (session == null || !session.isLoggedIn()) {
            printMessage("Session expired. Please login again.", RED);
            return false;
        }
        return true;
    }

    /**
     * Helper to print messages with colors.
     */
    private void printMessage(String message, String color) {
        System.out.println(color + message + RESET);
    }
}
