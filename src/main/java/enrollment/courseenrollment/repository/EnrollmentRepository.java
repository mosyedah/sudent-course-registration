package enrollment.courseenrollment.repository;

import java.util.List;

import enrollment.courseenrollment.model.Enrollment;

public interface EnrollmentRepository {
    boolean createEnrollment(Enrollment enrollment);
    List<Enrollment> getEnrollmentsByStudentId(String studentId);
    List<Enrollment> getWaitlistedEnrollmentsByCourseId(String courseId);
    Enrollment getEnrollmentByStudentAndCourse(String studentId, String courseId);// return null if not exists
    boolean updateEnrollment(Enrollment enrollment);
//    boolean deleteEnrollment(String enrollmentId); i'll update status instead of deleting
}
