package enrollment.courseenrollment.controller;

import java.util.Scanner;

/**
 * MainController handles the front desk menu of the console application.
 * It manages signup, login, and navigates to the student menu after authentication.
 */
public class MainController {

    private final StudentController studentController;
    private final CourseController courseController;
    private final Scanner scanner;

    public MainController(StudentController studentController, CourseController courseController) {
        this.studentController = studentController;
        this.courseController = courseController;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the application by showing the front desk menu repeatedly.
     */
    public void start() {
        while (true) {
            showFrontDeskMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": // Sign Up
                    studentController.signUp();
                    if (SessionManager.getInstance() != null && SessionManager.getInstance().isLoggedIn()) {
                        showStudentMenu();
                    }
                    break;

                case "2": // Login
                    studentController.login();
                    if (SessionManager.getInstance() != null && SessionManager.getInstance().isLoggedIn()) {
                        showStudentMenu();
                    }
                    break;

                case "3": // Exit
                    System.out.println("I’m well, Have a good day.");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showFrontDeskMenu() {
        System.out.println("\n=====================");
        System.out.println("Atlas Institute");
        System.out.println("=====================");
        System.out.println("Welcome to Front Desk, My Name is Aarti, How may I help?");
        System.out.println("1. I want to Signup as Student");
        System.out.println("2. I’m already a student, I want to login");
        System.out.println("3. I just came to check how are you, I’ll go now");
        System.out.print("Enter choice: ");
    }

    private void showStudentMenu() {
        SessionManager session = SessionManager.getInstance();
        while (session != null && session.isLoggedIn()) {
            System.out.println("\n--- Welcome, " + studentController.getLoggedInStudentName() + ", what can I do for you? ---");
            System.out.println("1. Show my Profile");
            System.out.println("2. Update my Profile");
            System.out.println("3. Change Password");
            System.out.println("4. What courses do you offer");
            System.out.println("5. Show my courses");
            System.out.println("6. I want to enroll in a course");
            System.out.println("7. I want to drop from a course");
            System.out.println("8. I’m all set, log me out");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    studentController.viewProfile();
                    break;
                case "2":
                    studentController.updateProfile();
                    break;
                case "3":
                    studentController.changePassword();
                    break;
                case "4":
                    courseController.viewCourses();
                    break;
                case "5":
                    courseController.showStudentCourses();
                    break;
                case "6":
                    courseController.enroll();
                    break;
                case "7":
                    courseController.drop();
                    break;
                case "8":
                    System.out.println("== Bye For now ==");
                    session.destroySession();
                    return; // go back to front desk menu
                default:
                    System.out.println("Invalid choice. Try again.");
            }

            // Refresh the session after each action to extend TTL
            if (SessionManager.getInstance() != null) {
//                SessionManager.getInstance().refreshSession();
            } else {
                System.out.println("Session expired. Please login again.");
                return;
            }
        }
    }
}
