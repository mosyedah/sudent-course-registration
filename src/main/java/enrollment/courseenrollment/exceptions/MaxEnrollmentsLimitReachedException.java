package enrollment.courseenrollment.exceptions;

public class MaxEnrollmentsLimitReachedException extends RuntimeException {
	public MaxEnrollmentsLimitReachedException(String message) {
		super(message);
	}
}
