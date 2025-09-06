package enrollment.courseenrollment.repository.dynamodb.constants;

public final class StudentTableConstants {
    private StudentTableConstants(){
    	// to avoid instantiation
    }
    
    public static final String TABLE_NAME = "Student";
    public static final String STUDENT_ID = "studentId";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PASSWORD_HASH = "passwordHash";
    public static final String EMAIL_INDEX = "EmailIndex";
}