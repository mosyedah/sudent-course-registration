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
	private static StudentService studentService;
	private static StudentRepository studentRepository;
	
	private static String password = "password";
	
	private static List<Student> students = new ArrayList<>();
	
	@BeforeAll
	static void setup(){
		studentRepository = new StudentRepoDynamoDb();
		LogRepository logRepository = new LogRepoDynamoDb();
		
		LogService logService = new LogService(logRepository);
		
		studentService = new StudentService(studentRepository, logService);
		
		
	}
	
	@AfterAll
	static void cleanupTestData() throws Exception {
		for (Student student : students) {
			((StudentRepoDynamoDb)studentRepository).deleteStudentById(student.getStudentId());
		}
		
	}
	
	 // ---------- signUp ----------
    @Test
    void testSignUpSuccess() { 
    	String email  ="testuniquenotexisting@example.com";
    	
    	Student student = getStudent(email);
    	
    	Student returned = studentService.signUp(student);
    	
    	
    	Assertions.assertNotNull(returned,"Student Object Returned was null ");
    	Assertions.assertEquals(email,returned.getEmail(),"Student Object Returned Email was Different");
    	Assertions.assertNotEquals(password, returned.getPasswordHash(), "password not hashed");
    }

    @Test
    void testSignUpDuplicateEmailThrowsException() { 
    	String email  ="TestExistingUserUnique@example.com";
    	
    	Student student = getStudent(email); 
    	
    	studentService.signUp(student);
    	
    	
    	Student newStudent = getStudent(email);
    	
    	Assertions.assertThrows(EmailAlreadyExistsException.class, ()-> {
    		studentService.signUp(newStudent);
    	}, "EmailAlreadyExists check failed");
    	
    }

    

    // ---------- login ----------
    @Test
    void testLoginSuccess() {
    	String email = "testloginsuccessmethod@example.com";
    	Student student = getStudent(email);
    	
    	studentService.signUp(student);
    	
    	Student returned = studentService.login(email, password);
    	
    	Assertions.assertNotNull(returned,"Student Object Returned was null ");
    	Assertions.assertEquals(email,returned.getEmail(),"Student Object Returned Email was Different");
    	Assertions.assertNotEquals(password, returned.getPasswordHash(), "password not hashed ");
    	
    }

    @Test
    void testLoginWithWrongPasswordThrowsInvalidCredentialsException() {
    	
	    String email  ="TestWrongPasswordUser@example.com";
	   
	    Student student = getStudent(email);
	    
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
    void testViewProfileSuccess() {
    	String email = "viewprofileemailsuccess@example.com";
    	String name = "View Profile";
    	Student student = getStudent(email,name);
    	
    	studentService.signUp(student);
    	
    	Student returned  = studentService.viewProfile(student.getStudentId());
    	
    	Assertions.assertEquals(email, returned.getEmail(),"Email not equal");
    	Assertions.assertEquals(name, returned.getName(),"Name not equal");
    }

    @Test
    void testViewProfileInvalidIdThrowsException() { 
    	String studentId = "Test Non Existing " + UUID.randomUUID().toString();
    	
    	Assertions.assertThrows(StudentNotFoundException.class, ()->{
    		studentService.viewProfile(studentId);
    	} , "Should throw StudentNotFoundException with invalid Id");
    	
    }

    // ---------- updateProfile ----------
    @Test
    void testUpdateProfileSuccess() { 
    	
    	String orgName = "Update Profile";
    	String orgEmail = "orgemailupdateprofile@example.com";

    	Student signedUp =  studentService.signUp(getStudent(orgEmail,orgName));
    	
    	
    	String newName = "Updated Profile";
    	String newEmail = "updatedemailupdateprofile@example.com";
    	
    	
    	Student updated = studentService.updateProfile(signedUp.getStudentId(),newName, newEmail);
    	
    	Assertions.assertEquals(newEmail, updated.getEmail(),"Email Mismatch after Update");
    	Assertions.assertEquals(newName, updated.getName(),"Name Mismatch after Update");
    	
    }

    @Test
    void testUpdateProfileWithDuplicateEmailThrowsException() {
    	String email = "DupemailUpdateProfileTest@example.com";
    	
    	 studentService.signUp(getStudent(email));
    	
    	Student student2 = studentService.signUp(getStudent("updatefailednewemal@gmail.com"));
    	Assertions.assertThrows(EmailAlreadyExistsException.class, ()->{
    		studentService.updateProfile(student2.getStudentId(), student2.getName(), email);
    	});
    	
    }

    // ---------- changePassword ----------
    @Test
    void testChangePasswordSuccess() { 
    	String email = "Passwordchangeemail@example.com";
    	
    	Student student = getStudent(email);
    	
    	studentService.signUp(student);
    	
    	String newPassword = "passwordNew";
    	
    	
    	Assertions.assertTrue(studentService.changePassword(student.getStudentId(), newPassword),
    			"password update failed");
    	
    	Assertions.assertNotNull(studentService.login(email, newPassword),"Login failed with new Password");
    }

    @Test
    void testChangePasswordFailsReturnsFalse() {
    	String  studentId = "Test User Non existing " + UUID.randomUUID().toString();
    	
    	Assertions.assertThrows(StudentNotFoundException.class, ()-> {
    		studentService.changePassword(studentId, password);
    	}, "Should throw Student Not found for Non existing Student Id");
    	
    }

    // ----- utility method
    
    private Student getStudent(String email , String name) {
    	String studentId = "Test User - "+ UUID.randomUUID().toString();
    	
    	Student student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setStudentId(studentId);
    	student.setPasswordHash(password);
    	
    	students.add(student);
    	
    	return student;
    }
    
    private Student getStudent(String email) {
    	String name = "Test User StudentServiceTest";
    	return getStudent(email,name);
    }
}
