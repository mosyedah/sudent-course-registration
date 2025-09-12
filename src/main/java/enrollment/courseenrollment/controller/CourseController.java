package enrollment.courseenrollment.controller;

import enrollment.courseenrollment.exceptions.CourseAlreadyAppliedException;
import enrollment.courseenrollment.exceptions.CourseEnrollmentDateHasPassedException;
import enrollment.courseenrollment.exceptions.CourseNotFoundException;
import enrollment.courseenrollment.exceptions.DropNotAllowedAfterCourseEndDateException;
import enrollment.courseenrollment.exceptions.DropNotAllowedForEnrollmentStatusException;
import enrollment.courseenrollment.exceptions.MaxEnrollmentsLimitReachedException;
import enrollment.courseenrollment.exceptions.MaxWaitlistedLimitReachedException;
import enrollment.courseenrollment.exceptions.StudentNotEnrolledForThisCourseException;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;
import enrollment.courseenrollment.service.CourseService;
import enrollment.courseenrollment.service.WaitlistService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
        System.out.println("\n" + CYAN + BOLD + "--- Available Courses ---" + RESET);

        if (courses == null || courses.isEmpty()) {
            System.out.println(RED + "No courses available, come back later." + RESET);
            return;
        }

        // Separate open and closed courses
        List<Course> openCourses = new ArrayList<>();
        List<Course> closedCourses = new ArrayList<>();

        for (Course c : courses) {
            if (isDateInFuture(c.getLatestEnrollmentBy())) {
                openCourses.add(c);
            } else {
                closedCourses.add(c);
            }
        }

        // Table header
        System.out.println(
                padWithColor(BOLD + YELLOW + "CourseID" + RESET, 12) +
                padWithColor(BOLD + YELLOW + "Course Name" + RESET, 28) +
                padWithColor(BOLD + YELLOW + "Seats" + RESET, 25) +
                padWithColor(BOLD + YELLOW + "Enrollment" + RESET, 15)
        );
        System.out.println("-------------------------------------------------------------------------------");

        // Print open courses first
        for (Course c : openCourses) {
            int seatsLeft = c.getMaxSeats() - c.getSeatsFilled();
            String seatInfo = (seatsLeft > 0)
                    ? seatsLeft + " available / " + c.getMaxSeats()
                    : YELLOW + "Waitlist" + RESET;
            String enrollmentStatus = GREEN + "Open" + RESET;

            System.out.println(
                    padWithColor(c.getCourseId(), 12) +
                    padWithColor(c.getCourseName(), 28) +
                    padWithColor(seatInfo, 25) +
                    padWithColor(enrollmentStatus, 15)
            );
        }

        // Print closed courses after
        for (Course c : closedCourses) {
            String seatInfo = RED + "Not Available" + RESET;
            String enrollmentStatus = RED + "Closed" + RESET;

            System.out.println(
                    padWithColor(c.getCourseId(), 12) +
                    padWithColor(c.getCourseName(), 28) +
                    padWithColor(seatInfo, 25) +
                    padWithColor(enrollmentStatus, 15)
            );
        }

    }


    /**
     * Shows student's courses based on type (enrolled, past, waitlisted, all).
     */
    public void showStudentCourses() {
        if (!isSessionActive()) return;

        String studentId = SessionManager.getInstance().getUserId();
        List<Enrollment> enrollments = courseService.getEnrollmentsByStudentId(studentId);

        if (enrollments.isEmpty()) {
            System.out.println("You have no courses yet.");
            return;
        }

        System.out.println("\n=== Select courses to display ===");
        System.out.println("1. Show Enrolled Courses only");
        System.out.println("2. Show Past Courses");
        System.out.println("3. Show Waitlisted Courses");
        System.out.println("4. Show All courses");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        enrollments.forEach(e -> {
        	EnrollmentStatus status = e.getStatus();
            switch (choice) {
                case "1":
                    if (status == EnrollmentStatus.ENROLLED)
                        printEnrollment(e);
                    break;
                case "2":
                    if (status == EnrollmentStatus.COMPLETED 
                    	|| status == EnrollmentStatus.DROPPED 
                    	|| status == EnrollmentStatus.WITHDRAWN
                    	|| status == EnrollmentStatus.OPTED_OUT)
                        printEnrollment(e);
                    break;
                case "3":
                    if (status == EnrollmentStatus.WAITLISTED)
                        printEnrollment(e);
                    break;
                case "4":
                    printEnrollment(e);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        });
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
				System.out.println("Successfully applied for Course " + courseId + " Refresh Page to check status.");
			} else {
				System.out.println("Enrollment failed. Seat Availability may have changed, refresh page."); 
			}
			
		} catch (CourseEnrollmentDateHasPassedException e) {
			System.out.println(e.getMessage());
		} catch (CourseAlreadyAppliedException e) {
			System.out.println(e.getMessage());
		}catch (MaxEnrollmentsLimitReachedException e) {
			System.out.println(e.getMessage());
		}catch (MaxWaitlistedLimitReachedException e) {
			System.out.println(e.getMessage());
		}catch (CourseNotFoundException e) {
			System.out.println(e.getMessage());
		}
    }

    /**
     * Drops student from a course.
     */
    public void drop() {
        if (!isSessionActive()) return;

        String studentId = SessionManager.getInstance().getUserId();
        System.out.print("Enter Course ID to drop: ");
        
//        System.out.println("If Course Is Enrolled and Date is before latestEnrollmentDate , Student will be withdrawn(Cant Reapply), seat is released");
//        System.out.println("If Course Is Waitlisted and Date is before latestEnrollmentDate , Student will be Opted Out (Can Reapply) , Async waitlist adjustment");
//        System.out.println("If Course Is Enrolled and Date is after latestEnrollmentDate , Student will be Dropped, Seat is not released");
//        
        String courseId = scanner.nextLine();
		try {
			boolean success = courseService.drop(studentId, courseId);
			if (success) {
				System.out.println("Course dropped successfully.");
			} else {
				System.out.println("Drop failed. Refresh and Try Again");
			}
			
		} catch (StudentNotEnrolledForThisCourseException e) {
			System.out.println(e.getMessage());
		}catch (DropNotAllowedAfterCourseEndDateException e) {
			System.out.println(e.getMessage());
		}catch (DropNotAllowedForEnrollmentStatusException e) {
			System.out.println(e.getMessage());
		}catch (CourseNotFoundException e) {
			System.out.println(e.getMessage());
		}
        
        
    }

    private void printEnrollment(Enrollment e) {
        System.out.println(e.getCourseId() + " - Status: " + e.getStatus() +
                (e.getStatus() == EnrollmentStatus.WAITLISTED ? ", Position in waitlist: " + e.getPositionInWaitlist() : ""));
    }

    /**
     * Checks if session is active. If not, prints message.
     */
    private boolean isSessionActive() {
        SessionManager session = SessionManager.getInstance();
        if (session == null || !session.isLoggedIn()) {
            System.out.println("Session expired. Please login again.");
            return false;
        }
        return true;
    }
    
    private boolean isDateInFuture(Instant instant) {
    	Instant now = Instant.now();
		
		// Convert both to LocalDate in UTC (or your preferred zone)
		LocalDate givenDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
		LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
		
		return givenDate.isAfter(today);
    }
    
    /**
     * Pads text to fixed width while ignoring ANSI color codes for alignment.
     */
    private String padWithColor(String text, int width) {
        String plain = text.replaceAll("\u001B\\[[;\\d]*m", ""); // strip ANSI
        int padding = Math.max(0, width - plain.length());
        return text + " ".repeat(padding);
    }
}
