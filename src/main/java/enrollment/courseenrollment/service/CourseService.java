package enrollment.courseenrollment.service;

import enrollment.courseenrollment.exceptions.CourseAlreadyAppliedException;
import enrollment.courseenrollment.exceptions.CourseEnrollmentDateHasPassedException;
import enrollment.courseenrollment.exceptions.DropNotAllowedAfterCourseEndDateException;
import enrollment.courseenrollment.exceptions.DropNotAllowedForEnrollmentStatusException;
import enrollment.courseenrollment.exceptions.MaxEnrollmentsLimitReachedException;
import enrollment.courseenrollment.exceptions.MaxWaitlistedLimitReachedException;
import enrollment.courseenrollment.exceptions.StudentNotEnrolledForThisCourseException;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.repository.CourseRepository;
import enrollment.courseenrollment.repository.EnrollmentRepository;
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
    private final WaitlistService waitlistService;

    // Constructor injection
    public CourseService(CourseRepository courseRepo, 
    		EnrollmentRepository enrollmentRepo,
    		LogService logService,
    		WaitlistService waitlistService) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.logService = logService;
        this.waitlistService = waitlistService;
    }

    // View all courses
    public List<Course> viewAllCourses() {
        return courseRepo.getAllCourses();
    }
    
    public Course getCourseById(String courseId) {
    	return courseRepo.getCourseById(courseId);
    }

    // Enroll student in course
    public boolean enroll(String studentId, String courseId) {
    	
    	// TODO: check if studentId+courseId exists , if exists check if it is optedOut proceed with PutItem
    	Course course = courseRepo.getCourseById(courseId);
    	if (!isDateInFuture(course.getLatestEnrollmentBy()))
    		throw new CourseEnrollmentDateHasPassedException("Course Already St : " + courseId);
    	
    	Enrollment enrollment  = enrollmentRepo.getEnrollmentByStudentAndCourse(studentId, courseId);
    	
    	if (enrollment != null) {
			if (enrollment.getStatus() != EnrollmentStatus.OPTED_OUT) {//opted out students can re apply
				throw new CourseAlreadyAppliedException("Course Already Applied CourseID :"+ courseId);
			}
		}
    	// TODO: if Dopped or completed or Waitlisted or Enrolled deny enrollment
    	
    	//constraints check, max 5 active courses, Max 3 waitlists per student 
    	
    	int waitlistCount = enrollmentRepo.getWaitlistedEnrollmentsByCourseId(courseId).size();
    	if (isSeatAvailable(course) && waitlistCount==0) {
			if (!canActiveEnrollStudent(studentId))
					throw new MaxEnrollmentsLimitReachedException("Max 5 courses can be actively enrolled");
			enrollment = new Enrollment();
			enrollment.setStudentId(studentId);
			enrollment.setCourseId(courseId);
			enrollment.setStatus(EnrollmentStatus.ENROLLED);
			enrollment.setEnrolledAt(Instant.now());
			if( !enrollmentRepo.createEnrollment(enrollment)) // updates course seatfill count as well, using Transaction
				return false;
			// logging 
			logEnrollment(enrollment, ActionType.ENROLL);
			
			return true;
		}else {
			if (!canWaitlistStudent(studentId)) 
				throw new MaxWaitlistedLimitReachedException("Max 3 course can be waitlisted");
			enrollment  = new Enrollment();
			enrollment.setStudentId(studentId);
			enrollment.setCourseId(courseId);
			enrollment.setStatus(EnrollmentStatus.WAITLISTED);
			enrollment.setPositionInWaitlist(waitlistCount+1);
			enrollment.setWaitlistedAt(Instant.now());
			
			if(!enrollmentRepo.createEnrollment(enrollment)) return false;
			
			logEnrollment(enrollment, ActionType.WAITLISTED);
			
			return true;
			
			
		}
    	
        // TODO: check seat availability, create Enrollment (ENROLLED or WAITLISTED)
        // TODO: save using enrollmentRepo.createEnrollment(...)
        // TODO: log action using logRepo.createLog(...)
    	
    }

    // Drop student from course
    public boolean drop(String studentId, String courseId) {
    	// TODO: fetch enrollment by studentId + courseId
    	Course course = courseRepo.getCourseById(courseId);
    	Enrollment enrollment = enrollmentRepo.getEnrollmentByStudentAndCourse(studentId, courseId);
    	// TODO: if not present throw error, if present status shoudl be Enrolled/waitlisted throw error
    	if (enrollment == null)
    		throw new StudentNotEnrolledForThisCourseException("You're Not enrolled in this course, CourseId : "+ courseId);
    	
    	if (!isDateInFuture(course.getEndDate())) 
    		throw new DropNotAllowedAfterCourseEndDateException("Drop Not Allowed As Course Already Ended, CourseId  : "+courseId);
    	
    	EnrollmentStatus status = enrollment.getStatus();
    	if (status != EnrollmentStatus.ENROLLED && status != EnrollmentStatus.WAITLISTED) {
    		throw new DropNotAllowedForEnrollmentStatusException("Drop Now Allowed for Current Status , CourseId : "+ courseId);
		}
    	
    	// TODO: update status to DROPPED/OPTEDOUT if student is currently enrolled / Waitlisted else throw exception
        ActionType actionType;
        
        switch (status) {
		case ENROLLED: 
			if (isDateInFuture(course.getLatestEnrollmentBy())) {
				enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
				enrollment.setWithdrawnAt(Instant.now());
				actionType = ActionType.WITHDRAWN;								
			}else {
				enrollment.setStatus(EnrollmentStatus.DROPPED);
				enrollment.setDroppedAt(Instant.now());
				actionType = ActionType.DROP;				
			}
			break;
		case WAITLISTED:
			enrollment.setStatus(EnrollmentStatus.OPTED_OUT);
			enrollment.setOptedOutAt(Instant.now());
			enrollment.setPositionInWaitlist(null);
			actionType = ActionType.OPTED_OUT;
			break;
		default:
			throw new DropNotAllowedForEnrollmentStatusException("Drop Not Allowed For Current Enrollment Status");	
        }
        
    	if( !enrollmentRepo.updateEnrollment(enrollment)) return false;
    	//notify of a drop and let the waitlist handle changes in async manner
    	waitlistService.notifyWaitlistServiceOfDrop(courseId , actionType);
        // TODO: log action
    	logEnrollment(enrollment, actionType);
    	return true;
    }
    
    // return list of courses enrolled by student
	public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
		// TODO Auto-generated method stub
		return enrollmentRepo.getEnrollmentsByStudentId(studentId);
	}

	
	private boolean isSeatAvailable(Course course) {
		return (course.getMaxSeats() - course.getSeatsFilled()) >0;
	}
	
	private boolean canActiveEnrollStudent(String studentId) {
	    // max 5 active enrolls
	    return enrollmentRepo.getEnrollmentCountByStudentIdAndStatus(studentId, EnrollmentStatus.ENROLLED) < 5;
	}

	private boolean canWaitlistStudent(String studentId) {
	   // max 3 waitlists
	    return enrollmentRepo.getEnrollmentCountByStudentIdAndStatus(studentId, EnrollmentStatus.WAITLISTED) < 3;
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
	
}
