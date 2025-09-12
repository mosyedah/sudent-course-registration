package enrollment.courseenrollment.controller;

import enrollment.courseenrollment.exceptions.*;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;
import enrollment.courseenrollment.service.CourseService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles all course-related actions for the logged-in student.
 * Uses singleton SessionManager to track current user session.
 */
public class CourseController {

    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BOLD = "\u001B[1m";

    private final CourseService courseService;
    private final Scanner scanner;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Shows all available courses with seat/waitlist info.
     */
    public void viewCourses() {
        if (!isSessionActive()) return;

        List<Course> courses = courseService.viewAllCourses();
        printSectionHeader("Available Courses ", CYAN);

        if (courses == null || courses.isEmpty()) {
            printError("No courses available, come back later.");
            return;
        }

        List<Course> openCourses = new ArrayList<>();
        List<Course> closedCourses = new ArrayList<>();
        for (Course c : courses) {
            if (isDateInFuture(c.getLatestEnrollmentBy())) {
                openCourses.add(c);
            } else {
                closedCourses.add(c);
            }
        }

        printTableHeader("CourseID", "Course Name", "Status", "Enrollment Ends", "Seats");

        // Open courses
        for (Course c : openCourses) {
            int seatsLeft = c.getMaxSeats() - c.getSeatsFilled();
            String seatInfo = (seatsLeft > 0)
                    ? GREEN + "✅ " + seatsLeft + "/" + c.getMaxSeats() + RESET
                    : YELLOW + "⏳ Waitlist" + RESET;

            printTableRow(
                    c.getCourseId(),
                    c.getCourseName(),
                    GREEN + "Open" + RESET,
                    formatDate(c.getLatestEnrollmentBy()),
                    seatInfo
            );
        }

        // Closed courses
        for (Course c : closedCourses) {
            printTableRow(
                    c.getCourseId(),
                    c.getCourseName(),
                    RED + "Closed" + RESET,
                    formatDate(c.getLatestEnrollmentBy()),
                    RED + "Not Available" + RESET
            );
        }
    }

    /**
     * Shows student's courses grouped by status.
     */
    public void showStudentCourses() {
        if (!isSessionActive()) return;

        String studentId = SessionManager.getInstance().getUserId();
        List<Enrollment> enrollments = courseService.getEnrollmentsByStudentId(studentId);

        if (enrollments.isEmpty()) {
            printError("You have no courses yet.");
            return;
        }

        printSectionHeader("Your Courses", CYAN);

        List<Enrollment> enrolled = new ArrayList<>();
        List<Enrollment> waitlisted = new ArrayList<>();
        List<Enrollment> past = new ArrayList<>();

        for (Enrollment e : enrollments) {
            switch (e.getStatus()) {
                case ENROLLED -> enrolled.add(e);
                case WAITLISTED -> waitlisted.add(e);
                default -> past.add(e);
            }
        }

        printEnrollmentSection("Currently Enrolled 📚", enrolled, GREEN);
        printEnrollmentSection("Waitlisted ⏳", waitlisted, YELLOW);
        printEnrollmentSection("Past Courses 🕒", past, RED);
    }

    private void printEnrollmentSection(String title, List<Enrollment> enrollments, String color) {
        if (enrollments.isEmpty()) return;

        System.out.println("\n" + color + BOLD + title + RESET);
        printTableHeader("CourseID", "Status", "Extra Info");

        for (Enrollment e : enrollments) {
            String extra = (e.getStatus() == EnrollmentStatus.WAITLISTED)
                    ? "Position: " + e.getPositionInWaitlist()
                    : "";
            printTableRow(
                    e.getCourseId(),
                    e.getStatus().toString(),
                    extra
            );
        }
    }

    /**
     * Enrolls student in a course or joins waitlist if full.
     */
    public void enroll() {
        if (!isSessionActive()) return;

        String studentId = SessionManager.getInstance().getUserId();
        System.out.print("Enter Course ID to enroll: ");
        String courseId = scanner.nextLine();

        try {
            boolean enrolled = courseService.enroll(studentId, courseId);
            if (enrolled) {
                printSuccess("Successfully applied for Course " + courseId + ". Refresh page to check status.");
            } else {
                printWarning("Enrollment failed. Seat availability may have changed, refresh page.");
            }

        } catch (CourseEnrollmentDateHasPassedException |
                 CourseAlreadyAppliedException |
                 MaxEnrollmentsLimitReachedException |
                 MaxWaitlistedLimitReachedException |
                 CourseNotFoundException e) {
            printError(e.getMessage());
        }
    }

    /**
     * Drops student from a course.
     */
    public void drop() {
        if (!isSessionActive()) return;

        String studentId = SessionManager.getInstance().getUserId();
        System.out.print("Enter Course ID to drop: ");
        String courseId = scanner.nextLine();

        try {
            boolean success = courseService.drop(studentId, courseId);
            if (success) {
                printSuccess("Course " + courseId + " dropped successfully.");
            } else {
                printWarning("Drop failed. Refresh and try again.");
            }

        } catch (StudentNotEnrolledForThisCourseException |
                 DropNotAllowedAfterCourseEndDateException |
                 DropNotAllowedForEnrollmentStatusException |
                 CourseNotFoundException e) {
            printError(e.getMessage());
        }
    }

    // ---------------- Utility Methods ----------------

    private boolean isSessionActive() {
        SessionManager session = SessionManager.getInstance();
        if (session == null || !session.isLoggedIn()) {
            printError("Session expired. Please login again.");
            return false;
        }
        return true;
    }

    private boolean isDateInFuture(Instant instant) {
        LocalDate givenDate = instant.atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate today = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate();
        return givenDate.isAfter(today);
    }

    private String formatDate(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.of("Asia/Kolkata"))
                .format(instant);
    }

    // --- Printing Helpers ---
    private void printSectionHeader(String title, String color) {
        String line = "═".repeat(80);
        System.out.println("\n" + color + BOLD + "╔" + line + "╗" + RESET);
        int padding = (80 - title.length()) / 2;
        String paddedTitle = " ".repeat(padding) + title + " ".repeat(padding);
        System.out.println(color + BOLD + "║" + paddedTitle + "║" + RESET);
        System.out.println(color + BOLD + "╚" + line + "╝" + RESET);
    }

    private void printTableHeader(String... columns) {
        for (String col : columns) {
            System.out.print(padWithColor(BOLD + YELLOW + col + RESET, 20));
        }
        System.out.println();
        System.out.println("-".repeat(columns.length * 20));
    }

    private void printTableRow(String... values) {
        for (String v : values) {
            System.out.print(padWithColor(v, 20));
        }
        System.out.println();
    }

    private String padWithColor(String text, int width) {
        String plain = text.replaceAll("\u001B\\[[;\\d]*m", ""); // strip ANSI
        int padding = Math.max(0, width - plain.length());
        return text + " ".repeat(padding);
    }

    private void printSuccess(String msg) {
        System.out.println(GREEN + BOLD + "[✔] " + msg + RESET);
    }

    private void printError(String msg) {
        System.out.println(RED + BOLD + "[✖] " + msg + RESET);
    }

    private void printWarning(String msg) {
        System.out.println(YELLOW + BOLD + "[!] " + msg + RESET);
    }

//    private void printInfo(String msg) {
//        System.out.println(CYAN + BOLD + "[i] " + msg + RESET);
//    }
}
