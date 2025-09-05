package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.repository.LogRepository;
import enrollment.courseenrollment.repository.StudentRepository;

public class StudentService {

    private final StudentRepository studentRepo;
    private final LogRepository logRepo;

    // Constructor injection (SOLID friendly)
    public StudentService(StudentRepository studentRepo, LogRepository logRepo) {
        this.studentRepo = studentRepo;
        this.logRepo = logRepo;
    }

    // Sign up a new student
    public void signUp(Student student) {
        // TODO: validation, call studentRepo.createStudent(student)
        // TODO: log action using logRepo.createLog(...)
    }

    // Login by email + password hash
    public Student login(String email, String passwordHash) {
        // TODO: fetch student by email, validate password hash
        return null;
    }

    // View profile
    public Student viewProfile(String studentId) {
        // TODO: call studentRepo.getStudentById(studentId)
        return null;
    }

    // Update profile
    public void updateProfile(Student student) {
        // TODO: call studentRepo.updateStudent(student)
        // TODO: log action
    }

    // Change password
    public void changePassword(String studentId, String newPasswordHash) {
        // TODO: fetch, update password, save
        // TODO: log action
    }
}
