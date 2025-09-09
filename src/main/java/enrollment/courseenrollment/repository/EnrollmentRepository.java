package enrollment.courseenrollment.repository;

import java.util.List;

import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;

public interface EnrollmentRepository {
    boolean createEnrollment(Enrollment enrollment);
    List<Enrollment> getEnrollmentsByStudentId(String studentId);
    List<Enrollment> getWaitlistedEnrollmentsByCourseId(String courseId);
    Enrollment getEnrollmentByStudentAndCourse(String studentId, String courseId);// return null if not exists
    int getEnrollmentCountByStudentIdAndStatus(String studentId,EnrollmentStatus status);
    boolean updateEnrollment(Enrollment enrollment);
//    boolean deleteEnrollment(String enrollmentId); i'll update status instead of deleting
}
