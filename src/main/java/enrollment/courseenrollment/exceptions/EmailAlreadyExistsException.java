package enrollment.courseenrollment.exceptions;

// used by StudentService
public class EmailAlreadyExistsException extends RuntimeException {
	public EmailAlreadyExistsException(String message) {
		super(message);
	}
}
