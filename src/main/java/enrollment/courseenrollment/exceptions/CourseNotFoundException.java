package enrollment.courseenrollment.exceptions;

public class CourseNotFoundException extends RuntimeException{
	public CourseNotFoundException(String message) {
		super(message);
	}
}
