package enrollment.courseenrollment.exceptions;

public class StudentNotEnrolledForThisCourseException extends RuntimeException {
	public StudentNotEnrolledForThisCourseException(String message) {
		super(message);
	}
}
