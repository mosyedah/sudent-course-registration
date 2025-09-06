package enrollment.courseenrollment.model;

public class Student {
    private String studentId;
    private String name;
    private String email;
    private String passwordHash;
    
    public Student() {
		// TODO Auto-generated constructor stub
	}
    public Student(Student other) {
    	if (other!=null) {
			this.studentId = other.studentId;
			this.name = other.name;
			this.email = other.email;
			this.passwordHash = other.passwordHash;
		}
    }
    
    
    
    // Getters & Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
}
