package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.StudentLog;

import enrollment.courseenrollment.repository.LogRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;


@Service
public class LogService {

    private final LogRepository logRepo;

    // Constructor injection
    public LogService(LogRepository logRepo) {
        this.logRepo = logRepo;
    }

    // not in sync ,try writing async version
    // Log an action for a student
//    public void logAction(StudentLog log) {
//        log.setLogId(UUID.randomUUID().toString());
//        log.setTimestamp(Instant.now());
//        logRepo.createLog(log);
//    }
    
    public void logAction(StudentLog log) {
        
        CompletableFuture.runAsync(() -> {
           asyncLogging(log);
        });
    }
    
    //method will work in background in different thread
    // so caller no need to wait for logs to be entered 
    // and exceptions will not interrupt main thread
    
    private void asyncLogging(StudentLog log) {
    	log.setLogId(UUID.randomUUID().toString());
    	log.setTimestamp(Instant.now());
    	logRepo.createLog(log);
    }

 
    

    // Get all logs for a student (no use for now)
    public List<StudentLog> getLogs(String studentId) {
        // TODO: call logRepo.getLogsByStudentId(studentId) if needed
        return null;
    }
}
