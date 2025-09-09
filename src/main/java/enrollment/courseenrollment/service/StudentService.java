package enrollment.courseenrollment.service;

import enrollment.courseenrollment.model.Student;

import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.model.enums.ActionType;
import enrollment.courseenrollment.exceptions.*;
import java.util.UUID;
import enrollment.courseenrollment.repository.StudentRepository;

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
    			if( studentRepo.getStudentByEmail(student.getEmail()) != null )
    				throw new EmailAlreadyExistsException("Email Already Exists");
    			student.setStudentId(UUID.randomUUID().toString());
    			studentRepo.createStudent(student);
    			String desc  = String.format("Name : %s , Email : %s", student.getName(), student.getEmail());
    			logRecord(student.getStudentId(), ActionType.SIGN_UP,desc);
    			return student;
    		} 
    		catch (DatabaseUnknownException e) {
    			throw new DatabaseUnknownException(e.getMessage());
    		}	
    }

    // Login by email + password hash
    public Student login(String email, String passwordHash) {
        // TODO: fetch student by email, validate password hash
			Student student = studentRepo.getStudentByEmail(email);
			if (student == null) 
				throw new StudentNotFoundException("Email Does Not Exist");
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
    		throw new InvalidCredentialsException("Invalid Student Id");
    	logRecord(studentId, ActionType.VIEW_PROFILE);
        return student;
    }

    // Update profile
    public Student updateProfile(Student student) {
        // TODO: call studentRepo.updateStudent(student)
        // TODO: log action
    	studentRepo.updateStudent(student);
    	String desc = String.format("Email  : %s , Name : %s",student.getEmail(), student.getName() );
    	logRecord(student.getStudentId(), ActionType.UPDATE_PROFILE,desc);
    	return student;
    }

    // Change password
    public boolean changePassword(String studentId, String newPasswordHash) {
        // TODO: fetch, update password, save
        // TODO: log action
    	
    	boolean isUpdated = studentRepo.updateStudentPassword(studentId, newPasswordHash);
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
    
}
