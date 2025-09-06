package enrollment.courseenrollment.repository.dynamodb.constants;

public class StudentLogTableConstants {
	private StudentLogTableConstants() {
		// prevent Instantiation
	}
	
	public static final String TABLE_NAME = "StudentLog";
	public static final String LOG_ID = "logId";
	public static final String STUDENT_ID = "studentId";
	public static final String COURSE_ID = "courseId";
	public static final String ACTION = "action";
	public static final String TIMESTAMP = "timestamp";
	public static final String DESCRIPTION = "description";
	public static final String STUDENT_ID_INDEX = "StudentIdIndex";
	
}
