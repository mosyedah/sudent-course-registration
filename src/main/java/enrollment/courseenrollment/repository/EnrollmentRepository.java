package enrollment.courseenrollment.repository;

import java.util.List;

import enrollment.courseenrollment.model.Enrollment;

public interface EnrollmentRepository {
    void createEnrollment(Enrollment enrollment);
    Enrollment getEnrollmentById(String enrollmentId);
    List<Enrollment> getEnrollmentsByStudentId(String studentId);
    List<Enrollment> getWaitlistedEnrollmentsByCourseId(String courseId);
    void updateEnrollment(Enrollment enrollment);
    void deleteEnrollment(String enrollmentId);
}
