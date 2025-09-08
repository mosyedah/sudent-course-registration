package enrollment.courseenrollment.model;

import java.time.Instant;

public class Course {
    private String courseId;
    private String courseName;
    private int maxSeats;
    private int seatsFilled;
    private Instant startDate;
    private Instant endDate;
    private Instant latestEnrollmentBy;

    // Getters & Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getMaxSeats() { return maxSeats; }
    public void setMaxSeats(int maxSeats) { this.maxSeats = maxSeats; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public Instant getLatestEnrollmentBy() { return latestEnrollmentBy; }
    public void setLatestEnrollmentBy(Instant latestEnrollmentBy) { this.latestEnrollmentBy = latestEnrollmentBy; }
	public int getSeatsFilled() {return seatsFilled;}
	public void setSeatsFilled(int seatsFilled) {this.seatsFilled = seatsFilled;}
}

