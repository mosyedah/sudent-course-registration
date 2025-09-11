package enrollment.courseenrollment.controller;

import enrollment.courseenrollment.exceptions.CourseAlreadyAppliedException;
import enrollment.courseenrollment.exceptions.CourseEnrollmentDateHasPassedException;
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

import java.util.List;
import java.util.Scanner;

/**
 * Handles all course-related actions for the logged-in student.
 * Uses singleton SessionManager to track current user session.
 */
public class CourseController {

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
        System.out.println("\n--- Available Courses ---");
        
        if (courses == null) {
			System.out.println("No courses available, Come back later Time");
			return;
		}
        
        for (Course c : courses) {
        	int seats = c.getMaxSeats() - c.getSeatsFilled();
            String seatInfo = seats>0
                    ? "Seats: " + seats + "available , of " + c.getMaxSeats()
                    : "Full";
            System.out.println(c.getCourseId() + " - " + c.getCourseName() + " (" + seatInfo + ")");
        }

        System.out.println("\nOptions: Enroll In a course | Go back to Home Screen | Logout");
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
		}
    }

    /**
     * Drops student from a course.
     */
    public void drop() {
        if (!isSessionActive()) return;

        String studentId = SessionManager.getInstance().getUserId();
        System.out.print("Enter Course ID to drop: ");
        
        System.out.println("If Course Is Enrolled and Date is before latestEnrollmentDate , Student will be withdrawn(Cant Reapply), seat is released");
        System.out.println("If Course Is Waitlisted and Date is before latestEnrollmentDate , Student will be Opted Out (Can Reapply) , Async waitlist adjustment");
        System.out.println("If Course Is Enrolled and Date is after latestEnrollmentDate , Student will be Dropped, Seat is not released");
        
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
}
