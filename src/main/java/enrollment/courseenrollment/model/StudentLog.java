package enrollment.courseenrollment.model;

import java.util.Date;

import enrollment.courseenrollment.model.enums.ActionType;

public class StudentLog {
    private String logId;
    private String studentId;
    private String courseId;
    private ActionType action;
    private Date timestamp;
    private String description;

    // Getters & Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
	
    public String getDescription() { return description;}
	public void setDescription(String description) { this.description = description;}
}
