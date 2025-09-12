package enrollment.courseenrollment.controller;

import java.util.Scanner;

public class MainController {

    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BOLD = "\u001B[1m";

    private final StudentController studentController;
    private final CourseController courseController;
    private final Scanner scanner;

    public MainController(StudentController studentController, CourseController courseController) {
        this.studentController = studentController;
        this.courseController = courseController;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the application with front desk menu.
     */
    public void start() {
        while (true) {
            printFrontDeskMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": // Sign Up
                    studentController.signUp();
                    if (isSessionActive()) showStudentMenu();
                    break;
                case "2": // Login
                    studentController.login();
                    if (isSessionActive()) showStudentMenu();
                    break;
                case "3": // Exit
                    printMessage(GREEN, "I’m well, Have a good day. Bye!");
                    System.exit(0);
                default:
                    printMessage(RED, "Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Prints the front desk menu.
     */
    private void printFrontDeskMenu() {
        System.out.println("\n" + CYAN + BOLD + "=====================" + RESET);
        System.out.println(CYAN + BOLD + "    Atlas Institute" + RESET);
        System.out.println(CYAN + BOLD + "=====================" + RESET);
        System.out.println("Welcome to Front Desk. My name is Aarti, how may I help?");
        System.out.println("1. I want to Signup as Student");
        System.out.println("2. I’m already a student, I want to login");
        System.out.println("3. I just came to check how are you, I’ll go now");
        System.out.print("Enter choice: ");
    }

    /**
     * Displays the student menu after successful login/signup.
     */
    private void showStudentMenu() {
        SessionManager session = SessionManager.getInstance();
        while (isSessionActive()) {
            System.out.println("\n" + CYAN + BOLD + "--- Welcome, " + studentController.getLoggedInStudentName() + " ---" + RESET);
            System.out.println("1. Show my Profile");
            System.out.println("2. Update my Profile");
            System.out.println("3. Change Password");
            System.out.println("4. What courses do you offer");
            System.out.println("5. Show my courses");
            System.out.println("6. Enroll in a course");
            System.out.println("7. Drop from a course");
            System.out.println("8. Log me out");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

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
                    printMessage(GREEN, "== Bye For now! ==");
                    session.destroySession();
                    return;
                default:
                    printMessage(RED, "Invalid choice. Try again.");
            }

            // Optional: refresh session TTL if your SessionManager supports it
            // SessionManager.getInstance().refreshSession();
        }
    }

    /**
     * Checks if session is active.
     */
    private boolean isSessionActive() {
        SessionManager session = SessionManager.getInstance();
        if (session == null || !session.isLoggedIn()) {
            printMessage(RED, "Session expired. Please login again.");
            return false;
        }
        return true;
    }

    /**
     * Prints a message with the given color.
     */
    private void printMessage(String color, String message) {
        System.out.println(color + message + RESET);
    }
}
