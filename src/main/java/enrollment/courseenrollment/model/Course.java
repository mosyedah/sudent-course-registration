package enrollment.courseenrollment.model;

import java.util.Date;

public class Course {
    private String courseId;
    private String courseName;
    private int maxSeats;
    private Date startDate;
    private Date endDate;
    private Date latestEnrollmentBy;

    // Getters & Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getMaxSeats() { return maxSeats; }
    public void setMaxSeats(int maxSeats) { this.maxSeats = maxSeats; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getLatestEnrollmentBy() { return latestEnrollmentBy; }
    public void setLatestEnrollmentBy(Date latestEnrollmentBy) { this.latestEnrollmentBy = latestEnrollmentBy; }
}

