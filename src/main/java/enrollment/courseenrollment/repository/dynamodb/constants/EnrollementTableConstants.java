package enrollment.courseenrollment.repository.dynamodb.constants;

public class EnrollementTableConstants {
	private EnrollementTableConstants() {
		// prevent Instance
	}
	
	public static final String TABLE_NAME = "Enrollment";
	public static final String COURSE_INDEX = "CourseIndex";
	public static final String STUDENT_ID = "studentId";
	public static final String COURSE_ID = "courseId";
	public static final String STATUS = "status";
	public static final String POSITION_WAITLIST = "positionWaitlist";
	public static final String WAITLISTED_AT = "waitlistedAt";
	public static final String ENROLLED_AT = "enrolledAt";
	public static final String DROPPED_AT = "droppedAt";
	public static final String OPTED_OUT_AT = "optedOutAt";
	
	
}
