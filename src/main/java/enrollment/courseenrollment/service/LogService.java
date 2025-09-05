package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.repository.LogRepository;
import java.util.List;

public class LogService {

    private final LogRepository logRepo;

    // Constructor injection
    public LogService(LogRepository logRepo) {
        this.logRepo = logRepo;
    }

    // Log an action for a student
    public void logAction(String studentId, String action, String courseId) {
        // TODO: create StudentLog object, set fields (studentId, action, courseId, timestamp)
        // TODO: call logRepo.createLog(...)
    }

    // Get all logs for a student (optional)
    public List<StudentLog> getLogs(String studentId) {
        // TODO: call logRepo.getLogsByStudentId(studentId) if needed
        return null;
    }
}
