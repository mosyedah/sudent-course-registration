package enrollment.courseenrollment.controller;

import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.service.StudentService;
import java.util.Scanner;

/**
 * Handles all student-related actions after signup/login.
 * Uses singleton SessionManager to check session and track current user.
 */
public class StudentController {

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
        System.out.println("\n--- I’ll definitely help you signing up, help me with details ---");

        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        if (!InputUtils.isValidName(name)) {
            System.out.println("Invalid name. Must be letters only, 2-50 chars.");
            return;
        }

        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        if (!InputUtils.isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (!InputUtils.isValidPassword(password)) {
            System.out.println("Password must be at least 6 characters.");
            return;
        }


        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPasswordHash(password);

        student = studentService.signUp(student);
        if (student!=null) {
            System.out.println("Thank you for details, your account is created and now you’re logged in.");
            this.loggedInStudent = student;
            SessionManager.createSession(student.getStudentId());
        } else {
            System.out.println("Signup failed. Email might already be registered.");
        }
    }

    /**
     * Logs in an existing student and starts a session if successful.
     */
    public void login() {
        System.out.println("\n--- Welcome back, help with details for logging in ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Student student = studentService.login(email, password);
        if (student != null) {
            System.out.println("Thank you for details, now you’re logged in.");
            this.loggedInStudent = student;
            SessionManager.createSession(student.getStudentId());
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    /**
     * Displays logged-in student's profile.
     */
    public void viewProfile() {
        if (!isSessionActive()) return;

        System.out.println("\n--- Your Profile Details ---");
        System.out.println("Name: " + loggedInStudent.getName());
        System.out.println("Email: " + loggedInStudent.getEmail());
    }

    /**
     * Updates the logged-in student's profile.
     */
    public void updateProfile() {
        if (!isSessionActive()) return;

        System.out.println("\n--- Help me with changes ---");
        System.out.print("Enter new name (or leave blank to keep current): ");
        String name = scanner.nextLine();
        if (!name.isBlank() && !InputUtils.isValidName(name)) {
            System.out.println("Invalid name. Update aborted.");
            return;
        }

        System.out.print("Enter new email (or leave blank to keep current): ");
        String email = scanner.nextLine();
        if (!email.isBlank() && !InputUtils.isValidEmail(email)) {
            System.out.println("Invalid email. Update aborted.");
            return;
        }
        Student student = new Student(loggedInStudent);
        student.setEmail(email.isBlank() ? loggedInStudent.getEmail() : email);
        student.setName(name.isBlank() ? loggedInStudent.getName() : name);
        try {
			
        	loggedInStudent = studentService.updateProfile(student);
		} catch (Exception e) {
			// TODO: handle exception
		}

       
    }

    /**
     * Changes the password of the logged-in student.
     */
    public void changePassword() {
        if (!isSessionActive()) return;

        System.out.println("\n--- Change Password ---");
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        if (!InputUtils.isValidPassword(newPassword)) {
            System.out.println("Password must be at least 6 characters. Update aborted.");
            return;
        }

        boolean success = studentService.changePassword(loggedInStudent.getStudentId(),
               newPassword);

        if (success) {
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Password change failed.");
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
            System.out.println("Session expired. Please login again.");
            return false;
        }
        return true;
    }
}

