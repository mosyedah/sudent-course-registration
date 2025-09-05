package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.repository.CourseRepository;
import enrollment.courseenrollment.repository.EnrollmentRepository;
import enrollment.courseenrollment.repository.LogRepository;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;
import java.util.List;

public class CourseService {

    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final LogRepository logRepo;

    // Constructor injection
    public CourseService(CourseRepository courseRepo, EnrollmentRepository enrollmentRepo, LogRepository logRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.logRepo = logRepo;
    }

    // View all courses
    public List<Course> viewAllCourses() {
        return courseRepo.getAllCourses();
    }

    // Enroll student in course
    public void enroll(String studentId, String courseId) {
        // TODO: check seat availability, create Enrollment (ENROLLED or WAITLISTED)
        // TODO: save using enrollmentRepo.createEnrollment(...)
        // TODO: log action using logRepo.createLog(...)
    }

    // Drop student from course
    public void drop(String studentId, String courseId) {
        // TODO: fetch enrollment by studentId + courseId
        // TODO: update status to DROPPED if student is currently enrolled else throw exception
        // TODO: update enrollmentRepo
        // TODO: log action
    }

    // Check if seats are available in a course
    public boolean checkSeatAvailability(String courseId) {
        // TODO: fetch course, count current ENROLLED enrollments, compare with maxSeats
        return false;
    }
}
