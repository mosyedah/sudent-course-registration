package enrollment.courseenrollment;


import enrollment.courseenrollment.controller.*;
import enrollment.courseenrollment.repository.*;
import enrollment.courseenrollment.repository.dynamodb.*;
import enrollment.courseenrollment.service.*;

/**
 * Hello world!
 */
public class App {
	private static MainController mainController;
	private static final String BOLD = "\u001B[1m";
	public static final String GREEN = "\u001B[32m";
	
    public static void main(String[] args) {
        System.out.println(GREEN+"Starting Programme....");
        setup();
        mainController.start();
        System.out.println("Programme Ended Without Errors");
    }
    
    
    private static void setup() {
    	
    	
    	// Repo Instances
    	System.out.println(GREEN+"Building Repos....");
    	
    	CourseRepository courseRepository = new CourseRepoDynamoDb(); 
    	EnrollmentRepository enrollmentRepository = new EnrollmentRepoDynamoDb();
    	StudentRepository studentRepository = new StudentRepoDynamoDb();
    	LogRepository logRepository = new LogRepoDynamoDb();
    	
    	
    	// Service Instances
    	System.out.println(GREEN+"Setting Up Services....");
    	
    	LogService logService = new LogService(logRepository);
    	WaitlistService waitlistService = new WaitlistService(enrollmentRepository, logService, courseRepository);
    	CourseService courseService = new CourseService(courseRepository, enrollmentRepository, logService, waitlistService);
    	StudentService studentService = new StudentService(studentRepository, logService);
    	
    	// Controller Instances
    	System.out.println(GREEN+"Setting Up Controllers....");
    	
    	StudentController studentController = new StudentController(studentService);
    	CourseController courseController = new CourseController(courseService);
    	mainController = new MainController(studentController, courseController);
    }
}
