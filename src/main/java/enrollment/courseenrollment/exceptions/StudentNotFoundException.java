package enrollment.courseenrollment.exceptions;

//used by studet service
public class StudentNotFoundException extends RuntimeException {
	public StudentNotFoundException(String message) {
		super(message);
	}
}
