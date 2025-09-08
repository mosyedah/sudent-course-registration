package enrollment.courseenrollment.repository;

import java.util.List;

import enrollment.courseenrollment.model.Enrollment;

public interface EnrollmentRepository {
    boolean createEnrollment(Enrollment enrollment);
    List<Enrollment> getEnrollmentsByStudentId(String studentId);
    List<Enrollment> getWaitlistedEnrollmentsByCourseId(String courseId);
    boolean updateEnrollment(Enrollment enrollment);
    boolean deleteEnrollment(String enrollmentId);
}
