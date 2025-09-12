package enrollment.courseenrollment;


import enrollment.courseenrollment.controller.CourseController;
import enrollment.courseenrollment.controller.MainController;
import enrollment.courseenrollment.controller.StudentController;
import enrollment.courseenrollment.repository.*;
import enrollment.courseenrollment.repository.dynamodb.*;
import enrollment.courseenrollment.service.*;

/**
 * Hello world!
 */
public class App {
	private static MainController mainController;
	
    public static void main(String[] args) {
        System.out.println("Booting services ....");
        setup();
        mainController.start();
    }
    
    
    private static void setup() {
    	
    	// Repo Instances
    	CourseRepository courseRepository = new CourseRepoDynamoDb(); 
    	EnrollmentRepository enrollmentRepository = new EnrollmentRepoDynamoDb();
    	StudentRepository studentRepository = new StudentRepoDynamoDb();
    	LogRepository logRepository = new LogRepoDynamoDb();
    	
    	
    	// Service Instances
    	LogService logService = new LogService(logRepository);
    	WaitlistService waitlistService = new WaitlistService(enrollmentRepository, logService, courseRepository);
    	CourseService courseService = new CourseService(courseRepository, enrollmentRepository, logService, waitlistService);
    	StudentService studentService = new StudentService(studentRepository, logService);
    	
    	// Controller Instances
    	StudentController studentController = new StudentController(studentService);
    	CourseController courseController = new CourseController(courseService);
    	mainController = new MainController(studentController, courseController);
    }
}
