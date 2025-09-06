package enrollment.courseenrollment.repository;

import enrollment.courseenrollment.model.Student;

public interface StudentRepository {
	void createStudent(Student student);
	Student getStudentById(String studentId);
	Student getStudentByEmail(String email);
	Student updateStudent(Student student);
}
