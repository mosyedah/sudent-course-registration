package enrollment.courseEnrollment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.*;

import enrollment.courseenrollment.exceptions.EmailAlreadyExistsException;
import enrollment.courseenrollment.exceptions.InvalidCredentialsException;
import enrollment.courseenrollment.exceptions.StudentNotFoundException;
import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.repository.LogRepository;
import enrollment.courseenrollment.repository.StudentRepository;
import enrollment.courseenrollment.repository.dynamodb.LogRepoDynamoDb;
import enrollment.courseenrollment.repository.dynamodb.StudentRepoDynamoDb;
import enrollment.courseenrollment.service.LogService;
import enrollment.courseenrollment.service.StudentService;

public class StudentServiceTest {
	private StudentService studentService;
	private StudentRepository studentRepository;
	
	private List<Student> students = new ArrayList<>();
	
	@BeforeAll
	void setup(){
		studentRepository = new StudentRepoDynamoDb();
		LogRepository logRepository = new LogRepoDynamoDb();
		
		LogService logService = new LogService(logRepository);
		
		studentService = new StudentService(studentRepository, logService);
		
		
	}
	
	@AfterAll
	void cleanupTestData() {
		 
		for (Student student : students) {
			((StudentRepoDynamoDb)studentRepository).deleteStudentById(student.getStudentId());
		}
		
	}
	
	 // ---------- signUp ----------
    @Test
    void testSignUpSuccess() { 
    	String email  ="testuniquenotExisting@example.com";
    	String name = "Test User Signup Method";
    	String studentId = "Test User - "+ UUID.randomUUID().toString();
    	String password = "password";
    	
    	Student student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setStudentId(studentId);
    	student.setPasswordHash(password);
    	
    	students.add(student);
    	
    	Student returned = studentService.signUp(student);
    	
    	
    	Assertions.assertNotNull(returned,"Student Object Returned was null ");
    	Assertions.assertEquals(email,returned.getEmail(),"Student Object Returned Email was Different");
    	Assertions.assertNotEquals(password, returned.getPasswordHash(), "password not hashed");
    }

    @Test
    void testSignUpDuplicateEmailThrowsException() { 
    	String email  ="TestExistingUser@example.com";
    	String name = "Test User Signup Dup Method";
    	String studentId = "Test User - "+ UUID.randomUUID().toString();
    	String password = "password";
    	
    	Student student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setStudentId(studentId);
    	student.setPasswordHash(password);
    	
    	students.add(student);
    	
    	Student returned = studentService.signUp(student);
    	
    	
    	Student newStudent = new Student();
    	newStudent.setEmail(email);
    	newStudent.setName(name);
    	newStudent.setStudentId("Test User - "+ UUID.randomUUID().toString());
    	newStudent.setPasswordHash(password);
    	
    	students.add(newStudent);
    	
    	Assertions.assertThrows(EmailAlreadyExistsException.class, ()-> {
    		studentService.signUp(newStudent);
    	}, "EmailAlreadyExists check failed");
    	
    }

    

    // ---------- login ----------
    @Test
    void testLoginSuccess() {
    	String email  ="TestLoginSuccessUser@example.com";
    	String name = "Test User Login Method";
    	String studentId = "Test User - "+ UUID.randomUUID().toString();
    	String password = "password";
    	
    	Student student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setStudentId(studentId);
    	student.setPasswordHash(password);
    	
    	students.add(student);
    	
    	studentService.signUp(student);
    	
    	Student returned = studentService.login(email, password);
    	
    	Assertions.assertNotNull(returned,"Student Object Returned was null ");
    	Assertions.assertEquals(email,returned.getEmail(),"Student Object Returned Email was Different");
    	Assertions.assertNotEquals(password, returned.getPasswordHash(), "password not hashed ");
    	
    }

    @Test
    void testLoginWithWrongPasswordThrowsInvalidCredentialsException() {
    	
	    String email  ="TestWrongPasswordUser@example.com";
	    String name = "Test User Login InvalidPassword Method";
	    String studentId = "Test User - "+ UUID.randomUUID().toString();
	    String password = "password";
	    
	    Student student = new Student();
	    student.setEmail(email);
	    student.setName(name);
	    student.setStudentId(studentId);
	    student.setPasswordHash(password);
	    
	    students.add(student);
	    
	    studentService.signUp(student);
	    
	    Assertions.assertThrows(InvalidCredentialsException.class, ()-> {
	    	studentService.login(email, "InvalidPassword");
	    }, "Password check failed");
    
    }

    @Test
    void testLoginWithUnknownEmailThrowsStudentNotFoundException() {
    	String  email = "NonexistingEmail@example.com";
    	
    	Assertions.assertThrows(StudentNotFoundException.class, ()-> {
    		studentService.login(email, "dummyPassword");
    	}, "Password check failed");
    }

    // ---------- viewProfile ----------
    @Test
    void testViewProfileSuccess() { }

    @Test
    void testViewProfileInvalidIdThrowsException() { }

    // ---------- updateProfile ----------
    @Test
    void testUpdateProfileSuccess() { }

    @Test
    void testUpdateProfileWithDuplicateEmailThrowsException() { }

    // ---------- changePassword ----------
    @Test
    void testChangePasswordSuccess() { }

    @Test
    void testChangePasswordFailsReturnsFalse() { }

}
