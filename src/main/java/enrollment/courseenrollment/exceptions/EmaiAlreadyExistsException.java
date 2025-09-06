package enrollment.courseenrollment.exceptions;

// used by StudentService
public class EmaiAlreadyExistsException extends RuntimeException {
	public EmaiAlreadyExistsException(String message) {
		super(message);
	}
}
