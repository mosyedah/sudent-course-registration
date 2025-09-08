package enrollment.courseenrollment.repository.dynamodb.constants;

public final class CourseTableConstants {
	private CourseTableConstants() {
		//avoid instantiation
	}
	
	public static final String TABLE_NAME = "Course";
	public static final String COURSE_ID = "courseId";
	public static final String COURSE_NAME = "maxSeats";
	public static final String MAX_SEATS = "maxSeats";
	public static final String SEATS_FILLED = "seatsFilled";
	public static final String ENROLLED_COUNT = "currentEnrolledCount";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String ENROLL_BY_DATE = "latestEnrollmentBy";
	
}
