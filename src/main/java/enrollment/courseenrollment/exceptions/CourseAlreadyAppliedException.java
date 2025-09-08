package enrollment.courseenrollment.exceptions;

public class CourseAlreadyAppliedException extends RuntimeException{
	public CourseAlreadyAppliedException(String message) {
		super(message);
	}
}
