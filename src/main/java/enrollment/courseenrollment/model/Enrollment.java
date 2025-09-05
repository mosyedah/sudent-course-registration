package enrollment.courseenrollment.model;

import java.util.Date;

import enrollment.courseenrollment.model.enums.EnrollmentStatus;

public class Enrollment {
    private String enrollmentId;
    private String studentId;
    private String courseId;
    private EnrollmentStatus status;
    private int positionInWaitlist;
    private Date waitlistedAt;
    private Date enrolledAt;

    // Getters & Setters
    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public int getPositionInWaitlist() { return positionInWaitlist; }
    public void setPositionInWaitlist(int positionInWaitlist) { this.positionInWaitlist = positionInWaitlist; }

    public Date getWaitlistedAt() { return waitlistedAt; }
    public void setWaitlistedAt(Date waitlistedAt) { this.waitlistedAt = waitlistedAt; }

    public Date getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Date enrolledAt) { this.enrolledAt = enrolledAt; }
}

