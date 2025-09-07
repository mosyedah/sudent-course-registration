package enrollment.courseenrollment.exceptions;

public class StudentIDAlreadyExistsException extends RuntimeException {
	public StudentIDAlreadyExistsException(String message) {
		super(message);
	}
}
