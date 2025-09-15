package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.Student;

import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.model.enums.ActionType;
import enrollment.courseenrollment.exceptions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import enrollment.courseenrollment.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final LogService logService;

    // Constructor injection (SOLID friendly)
    public StudentService(StudentRepository studentRepo, LogService logService) {
        this.studentRepo = studentRepo;
        this.logService = logService;
    }

    // Sign up a new student . Throws EMailAlreadyExistsException if email exists 
    public Student signUp(Student student) {
    	// TODO: validation, call studentRepo.createStudent(student)
    	// TODO: add Student ID unique to student.studentId
    	// TODO: log action using logRepo.createLog(...)
    		try {
    			if( studentRepo.getStudentByEmail(student.getEmail().toLowerCase()) != null )
    				throw new EmailAlreadyExistsException("Email Already Exists");
    			student.setStudentId(UUID.randomUUID().toString());
    			String passwordHash = hashPassword(student.getPasswordHash());
    			student.setPasswordHash(passwordHash);
    			String email = student.getEmail().toLowerCase();
    			student.setEmail(email);
    			studentRepo.createStudent(student);
    			String desc  = String.format("Name : %s , Email : %s", student.getName(), student.getEmail());
    			logRecord(student.getStudentId(), ActionType.SIGN_UP,desc);
    			return student;
    		} 
    		catch (DatabaseUnknownException e) {
    			throw new DatabaseUnknownException(e.getMessage());
    		}	
    }
    
    public Student getStudentbyId(String studentId) {
    	return studentRepo.getStudentById(studentId);
    }

    // Login by email + password hash
    public Student login(String email, String password) {
        // TODO: fetch student by email, validate password hash
    		email = email.toLowerCase();
			Student student = studentRepo.getStudentByEmail(email);
			if (student == null) 
				throw new StudentNotFoundException("Email Does Not Exist");
			String passwordHash = hashPassword(password);
			if (passwordHash.equals(student.getPasswordHash())) { 
				logRecord(student.getStudentId(), ActionType.LOGIN);
				return student;
			}
			else throw new InvalidCredentialsException("Password is Incorrect");
			
    }

    // View profile
    public Student viewProfile(String studentId) {
        // TODO: call studentRepo.getStudentById(studentId)
    	Student student = studentRepo.getStudentById(studentId);
    	if (student == null )
    		throw new StudentNotFoundException("Invalid Student Id");
    	logRecord(studentId, ActionType.VIEW_PROFILE);
        return student;
    }

//    // Update profile
//    public Student updateProfile(Student student , boolean isEmailUpdate) {
//        // TODO: call studentRepo.updateStudent(student)
//        // TODO: log action
//    	if (isEmailUpdate && studentRepo.getStudentByEmail(student.getEmail().toLowerCase()) != null)
//    		throw new EmailAlreadyExistsException("Email Already Exists, Failed to Update profile");
//    	
//    	String email = student.getEmail().toLowerCase();
//    	student.setEmail(email);
//    	
//    	studentRepo.updateStudent(student);
//    	String desc = String.format("Email  : %s , Name : %s",student.getEmail(), student.getName() );
//    	logRecord(student.getStudentId(), ActionType.UPDATE_PROFILE,desc);
//    	return student;
//    }

    public Student updateProfile(String studentId, String name, String email) {
    	email = email.toLowerCase();
    	Student student = studentRepo.getStudentByEmail(email);
    	if (student!=null && !studentId.equals(student.getStudentId())) {
			throw new EmailAlreadyExistsException("Email Already Exists, Failed to Update profile");
		}
    	student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setStudentId(studentId);
    	
    	Student returned = studentRepo.updateStudent(student);
    	String desc = String.format("Email  : %s , Name : %s",returned.getEmail(), returned.getName() );
    	logRecord(student.getStudentId(), ActionType.UPDATE_PROFILE,desc);
    	return returned;

    }
    
    
    // Change password
    public boolean changePassword(String studentId, String newPassword) {
        // TODO: fetch, update password, save
        // TODO: log action
    	
    	
    	boolean isUpdated = studentRepo.updateStudentPassword(studentId, hashPassword(newPassword));
    	if (isUpdated) {
			logRecord(studentId, ActionType.PASSWORD_UPDATE);
			return true;
		}else return false;
    }
    
    private void logRecord(String studentId , ActionType action) {
    	logRecord(studentId, action, null);
    }
    private void logRecord(String studentId , ActionType action, String desc) {
    	StudentLog log = new StudentLog();
    	log.setStudentId(studentId);
    	log.setAction(action);
    	log.setDescription(desc);
    	logService.logAction(log);
    }
    
 // Hash password using SHA-256
    private static String hashPassword(String password) {
        if (password == null) return null;
        String salt = "saltKey"; // can mix with userId also if needed 
        String combo = password+salt;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combo.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available");
        }
    }
    
}
