package enrollment.courseenrollment.repository;

import enrollment.courseenrollment.model.Student;

public interface StudentRepository {
	boolean createStudent(Student student);
	Student getStudentById(String studentId);
	Student getStudentByEmail(String email);
	Student updateStudent(Student student); // email, name 
	boolean updateStudentPassword(String studentId, String passwordHash); 
}
