package enrollment.courseenrollment.exceptions;

public class DropNotAllowedAfterCourseEndDateException extends RuntimeException{
	public DropNotAllowedAfterCourseEndDateException(String message) {
		super(message);
	}
}
