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
    private final LogService logService;

    // Constructor injection
    public CourseService(CourseRepository courseRepo, EnrollmentRepository enrollmentRepo, LogService logService) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.logService = logService;
    }

    // View all courses
    public List<Course> viewAllCourses() {
        return courseRepo.getAllCourses();
    }

    // Enroll student in course
    public boolean enroll(String studentId, String courseId) {
    	
    	// TODO: check if studentId+courseId exists , if exists check if it is optedOut proceed with PutItem
    	// TODO: if Dopped or completed or Waitlisted or Enrolled deny enrollment 
        // TODO: check seat availability, create Enrollment (ENROLLED or WAITLISTED)
        // TODO: save using enrollmentRepo.createEnrollment(...)
        // TODO: log action using logRepo.createLog(...)
    	return false;
    }

    // Drop student from course
    public boolean drop(String studentId, String courseId) {
    	// TODO: fetch enrollment by studentId + courseId
        // TODO: if not present throw error, if present status shoudl be Enrolled/waitlisted throw error
        // TODO: update status to DROPPED/OPTEDOUT if student is currently enrolled / Waitlisted else throw exception
        // TODO: update enrollmentRepo
        // TODO: log action
    	return false;
    }

    // Check if seats are available in a course
    public boolean checkSeatAvailability(String courseId) {
        // TODO: fetch course, count current ENROLLED enrollments, compare with maxSeats
        return false;
    }
    
    // return list of courses enrolled by student
	public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAvailableSeats(String courseId) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	// Process when a seat opens in a course
    public void processSeatOpen(String courseId) {
        // TODO: fetch waitlisted enrollments by courseId
        // TODO: promote first in waitlist to ENROLLED
        // TODO: update positions of remaining waitlisted students
        // TODO: save updates via enrollmentRepo.updateEnrollment(...)
        // TODO: log actions
    }
}
