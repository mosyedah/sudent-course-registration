package enrollment.courseenrollment.model;

import java.time.Instant;
import java.util.Date;

import enrollment.courseenrollment.model.enums.EnrollmentStatus;

public class Enrollment {
    private String studentId;
    private String courseId;
    private EnrollmentStatus status;
    private Integer positionInWaitlist;
    private Instant waitlistedAt;
    private Instant enrolledAt;
    private Instant droppedAt;
    private Instant optedOutAt;


    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(String status) { this.status = EnrollmentStatus.valueOf(status); }

    public Integer getPositionInWaitlist() { return positionInWaitlist; }
    public void setPositionInWaitlist(int positionInWaitlist) { this.positionInWaitlist = positionInWaitlist; }

    public Instant getWaitlistedAt() { return waitlistedAt; }
    public void setWaitlistedAt(String waitlistedAt) { this.waitlistedAt = Instant.parse(waitlistedAt); }

    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(String enrolledAt) { this.enrolledAt = Instant.parse(enrolledAt); }
	public Instant getDroppedAt() {
		return droppedAt;
	}
	public void setDroppedAt(String droppedAt) {
		this.droppedAt = Instant.parse(droppedAt);
	}
	public Instant getOptedOutAt() {
		return optedOutAt;
	}
	public void setOptedOutAt(String optedOutAt) {
		this.optedOutAt = Instant.parse(optedOutAt);
	}
}

