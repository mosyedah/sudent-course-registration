package enrollment.courseenrollment.service;

import enrollment.courseenrollment.exceptions.CourseAlreadyAppliedException;
import enrollment.courseenrollment.exceptions.CourseStartDateHasPassedException;
import enrollment.courseenrollment.exceptions.MaxEnrollmentsLimitReachedException;
import enrollment.courseenrollment.exceptions.MaxWaitlistedLimitReachedException;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.repository.CourseRepository;
import enrollment.courseenrollment.repository.EnrollmentRepository;
import enrollment.courseenrollment.repository.LogRepository;
import enrollment.courseenrollment.model.enums.ActionType;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;

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
    	Course course = courseRepo.getCourseById(courseId);
    	if (isDateInFuture(course.getStartDate()))
    		throw new CourseStartDateHasPassedException("Course Start Date Passed CourseID : " + courseId);
    	Enrollment enrollment  = enrollmentRepo.getEnrollmentByStudentAndCourse(studentId, courseId);
    	if (enrollment != null) {
			if (enrollment.getStatus() != EnrollmentStatus.OPTED_OUT) {
				throw new CourseAlreadyAppliedException("Course Already Applied CourseID :"+ courseId);
			}
		}
    	// TODO: if Dopped or completed or Waitlisted or Enrolled deny enrollment
    	
    	//constraints check, max 5 active courses, Max 3 waitlists per student 
    	
    	List<Enrollment> studentEnrollments = enrollmentRepo.getEnrollmentsByStudentId(studentId);
    	if (isSeatAvailable(course)) {
			if (!canActiveEnrollStudent(studentEnrollments))
					throw new MaxEnrollmentsLimitReachedException("Max 5 courses can be actively enrolled");
			enrollment = new Enrollment();
			enrollment.setStudentId(studentId);
			enrollment.setCourseId(courseId);
			enrollment.setStatus(EnrollmentStatus.ENROLLED);
			enrollment.setEnrolledAt(Instant.now());
			enrollmentRepo.createEnrollment(enrollment);
			
			// logging 
			logEnrollment(enrollment, ActionType.ENROLL);
			
			return true;
		}else {
			if (!canWaitlistStudent(studentEnrollments)) 
				throw new MaxWaitlistedLimitReachedException("Max 3 course can be waitlisted");
			enrollment  = new Enrollment();
			enrollment.setStudentId(studentId);
			enrollment.setCourseId(courseId);
			enrollment.setStatus(EnrollmentStatus.WAITLISTED);
			enrollment.setStatus(EnrollmentStatus.WAITLISTED);
		}
    	
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
	
	private boolean isSeatAvailable(Course course) {
		return (course.getMaxSeats() - course.getSeatsFilled()) >0;
	}
	
	private boolean canActiveEnrollStudent(List<Enrollment> enrollments) {
	    int count = 0;
	    for (Enrollment e : enrollments) {
	        if (e.getStatus() == EnrollmentStatus.ENROLLED) {
	            count++;
	            if (count >= 5) return false;
	        }
	    }
	    return true;
	}

	private boolean canWaitlistStudent(List<Enrollment> enrollments) {
	    int count = 0;
	    for (Enrollment e : enrollments) {
	        if (e.getStatus() == EnrollmentStatus.WAITLISTED) {
	            count++;
	            if (count >= 3) return false;
	        }
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
	
	private void logEnrollment(Enrollment e,ActionType action) {
		StudentLog log = new StudentLog();
		log.setStudentId(e.getStudentId());
		log.setCourseId(e.getCourseId());
		log.setAction(action);
		logService.logAction(log);
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
