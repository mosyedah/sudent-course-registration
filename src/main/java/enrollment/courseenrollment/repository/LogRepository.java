package enrollment.courseenrollment.repository;



import enrollment.courseenrollment.model.StudentLog;

public interface LogRepository {
	void createLog(StudentLog log);

}
