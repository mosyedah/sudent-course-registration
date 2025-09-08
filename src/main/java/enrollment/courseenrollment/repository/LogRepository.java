package enrollment.courseenrollment.repository;

import java.util.List;

import enrollment.courseenrollment.model.StudentLog;

public interface LogRepository {
	void createLog(StudentLog log);

}
