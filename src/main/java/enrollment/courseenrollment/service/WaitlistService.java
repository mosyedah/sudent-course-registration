package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.repository.EnrollmentRepository;
import enrollment.courseenrollment.repository.LogRepository;
import java.util.List;

public class WaitlistService {

    private final EnrollmentRepository enrollmentRepo;
    private final LogRepository logRepo;

    // Constructor injection
    public WaitlistService(EnrollmentRepository enrollmentRepo, LogRepository logRepo) {
        this.enrollmentRepo = enrollmentRepo;
        this.logRepo = logRepo;
    }

    // Join waitlist for a course
    public void joinWaitlist(String studentId, String courseId) {
        // TODO: create Enrollment with status WAITLISTED
        // TODO: determine position in waitlist
        // TODO: save using enrollmentRepo.createEnrollment(...)
        // TODO: log action
    }

    // Process when a seat opens in a course
    public void processSeatOpen(String courseId) {
        // TODO: fetch waitlisted enrollments by courseId
        // TODO: promote first in waitlist to ENROLLED
        // TODO: update positions of remaining waitlisted students
        // TODO: save updates via enrollmentRepo.updateEnrollment(...)
        // TODO: log actions
    }

    // Remove a student from waitlist
    public void removeFromWaitlist(String studentId, String courseId) {
        // TODO: fetch enrollment with WAITLISTED status
        // TODO: delete using enrollmentRepo.deleteEnrollment(...) or update status
        // TODO: log action
    }
}
