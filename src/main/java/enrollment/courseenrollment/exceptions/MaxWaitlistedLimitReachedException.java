package enrollment.courseenrollment.exceptions;

public class MaxWaitlistedLimitReachedException extends RuntimeException{
	public MaxWaitlistedLimitReachedException(String message) {
		super(message);
	}
}
