package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.enums.ActionType;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;
import enrollment.courseenrollment.repository.CourseRepository;
import enrollment.courseenrollment.repository.EnrollmentRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WaitlistService {

    private final EnrollmentRepository enrollmentRepo;
    private final LogService logService;
    private final CourseRepository courseRepo;
    

    public WaitlistService(EnrollmentRepository enrollmentRepo,
    		LogService logService , CourseRepository courseRepo) {
        this.enrollmentRepo = enrollmentRepo;
        this.logService = logService;
		this.courseRepo = courseRepo;
    }

    /**
     * Called asynchronously from CourseService when a student drops or opts out of waitlist.
     */
    public void notifyWaitlistServiceOfDrop(String courseId, ActionType actionType) {
        CompletableFuture.runAsync(() -> processWaitlistChange(courseId, actionType));
    }

    /**
     * Core async logic for handling waitlist changes.
     */
    private void processWaitlistChange(String courseId, ActionType actionType) {
        try {
            if (actionType == ActionType.OPTED_OUT) {
                // If student opted out, just update remaining waitlist positions
                updateWaitlistPositions(courseId);
            } else if (actionType == ActionType.WITHDRAWN) {
                // Promote first waitlisted student to ENROLLED
                List<Enrollment> waitlist = enrollmentRepo.getWaitlistedEnrollmentsByCourseId(courseId);
                Course course = courseRepo.getCourseById(courseId);
                
                if (course.getMaxSeats() <= course.getSeatsFilled()) 
					return;
				
                
                if (!waitlist.isEmpty()) {
                    Enrollment first = waitlist.get(0);
                    first.setStatus(EnrollmentStatus.ENROLLED);
                    first.setEnrolledAt(Instant.now());
                    first.setPositionInWaitlist(null);

                    // Transaction: update enrollment + increment seatsFilled in course
                    try {
                       if(! enrollmentRepo.updateEnrollment(first) ) 
                    	   return; //notify admin of failure out f scope
                    	    
                        logEnrollment(first, ActionType.ENROLLED_FROM_WAITLIST);
                    } catch (Exception e) {
                        // TODO: notify admin about transaction failure -out of scope
                    }

                    // Update remaining waitlist positions
                    updateWaitlistPositions(courseId);
                }
            }
        } catch (Exception e) {
            // TODO: //notify admin of failure out f scope
        }
    }

    /**
     * Updates positions of remaining waitlisted students.
     */
    private void updateWaitlistPositions(String courseId) {
        List<Enrollment> waitlist = enrollmentRepo.getWaitlistedEnrollmentsByCourseId(courseId);
        
        // sort by pos
        waitlist.sort(Comparator.comparingInt(e -> 
        		e.getPositionInWaitlist() == null ? Integer.MAX_VALUE : e.getPositionInWaitlist()
        		));
        
        int position = 1;
        for (Enrollment e : waitlist) {
            e.setPositionInWaitlist(position++);
            enrollmentRepo.updateEnrollment(e); // simple update, transaction not strictly needed here
        }
    }

    private void logEnrollment(Enrollment enrollment, ActionType actionType) {
        
    	StudentLog log = new StudentLog();
    	log.setAction(actionType);
    	log.setCourseId(enrollment.getCourseId());
    	log.setStudentId(enrollment.getStudentId());
        logService.logAction(log);
        
       
    }
}
