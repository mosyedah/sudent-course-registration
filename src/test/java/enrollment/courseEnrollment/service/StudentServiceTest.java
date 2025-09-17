package enrollment.courseEnrollment.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
	@Mock
	private StudentRepository studentRepository;
	
	@Mock
	private LogRepository logRepository;
	
	@Mock
	private LogService logService;
	
	private static String password = "password";
	
	@InjectMocks
	private  StudentService studentService;
//	@BeforeAll
//	static void setup(){
//		studentRepository = new StudentRepoDynamoDb();
//		LogRepository logRepository = new LogRepoDynamoDb();
//		
//		LogService logService = new LogService(logRepository);
//		
//		studentService = new StudentService(studentRepository, logService);
//		
//		
//	}
//	
//	@AfterAll
//	static void cleanupTestData() throws Exception {
//		for (Student student : students) {
//			((StudentRepoDynamoDb)studentRepository).deleteStudentById(student.getStudentId());
//		}
//		
//	}
//	
//	 // ---------- signUp ----------
    @Test
    void testSignUpSuccess() { 
    	String email  ="testuniquenotexisting@example.com";
    	
    	
//    	when(studentRepository.getStudentByEmail(email)).thenReturn(null);
    	
    	Student student = getStudent(email);
    	
    	Student returned = studentService.signUp(student);
    	
    	
    	Assertions.assertNotNull(returned,"Student Object Returned was null ");
    	Assertions.assertEquals(email,returned.getEmail(),"Student Object Returned Email was Different");
    	Assertions.assertNotEquals(password, returned.getPasswordHash(), "password not hashed");
    }
//
    @Test
    void testSignUpDuplicateEmailThrowsException() { 
    	String email  ="testexistinguserunique@example.com";
    	
    	Student student = getStudent(email); 
    	
    	when(studentRepository.getStudentByEmail(email)).thenReturn(student);
    	
    	
    	Assertions.assertThrows(EmailAlreadyExistsException.class, ()-> {
    		studentService.signUp(student);
    	}, "EmailAlreadyExists check failed");
    	
    }
//
//    
//
    // ---------- login ----------
    @Test
    void testLoginSuccess() {
    	String email = "testloginsuccessmethod@example.com";
    	Student student = getStudent(email);
    	
    	String hashPassword = "947b8d55227cf05466f40c26f059269705f266bf24ced2f4ad9731dabbcc3e93";
    	
    	student.setPasswordHash(hashPassword);
    	
    	when(studentRepository.getStudentByEmail(email)).thenReturn(student);
    	
    	Student returned = studentService.login(email, password);
    	
    	Assertions.assertNotNull(returned,"Student Object Returned was null ");
    	Assertions.assertEquals(email,returned.getEmail(),"Student Object Returned Email was Different");
    	Assertions.assertNotEquals(password, returned.getPasswordHash(), "password not hashed ");
    	
    }

    @Test
    void testLoginWithWrongPasswordThrowsInvalidCredentialsException() {
    	
	    String email  ="testwrongpassworduser@example.com";
	   
	    Student student = getStudent(email);
	    
	    when(studentRepository.getStudentByEmail(email)).thenReturn(student);
	    
	    Assertions.assertThrows(InvalidCredentialsException.class, ()-> {
	    	studentService.login(email, "InvalidPassword");
	    }, "Password check failed");
    
    }

    @Test
    void testLoginWithUnknownEmailThrowsStudentNotFoundException() {
    	String  email = "nonexistingemail@example.com";
    	
    	when(studentRepository.getStudentByEmail(email)).thenReturn(null);
    	
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
    	
    	when(studentRepository.getStudentById(student.getStudentId())).thenReturn(student);
    	
    	Student returned  = studentService.viewProfile(student.getStudentId());
    	
    	Assertions.assertEquals(email, returned.getEmail(),"Email not equal");
    	Assertions.assertEquals(name, returned.getName(),"Name not equal");
    }

    @Test
    void testViewProfileInvalidIdThrowsException() { 
    	String studentId = "Test Non Existing " + UUID.randomUUID().toString();
    	
    	when(studentRepository.getStudentById(studentId)).thenReturn(null);
    	
    	Assertions.assertThrows(StudentNotFoundException.class, ()->{
    		studentService.viewProfile(studentId);
    	} , "Should throw StudentNotFoundException with invalid Id");
    	
    }

    // ---------- updateProfile ----------
    @Test
    void testUpdateProfileSuccess() { 
    	String newName = "Updated Profile";
    	String newEmail = "updatedemailupdateprofile@example.com";
    	
    	when(studentRepository.getStudentByEmail(newEmail)).thenReturn(null);
    	
    	Student student = getStudent(newEmail,newName);
    	
    	when(studentRepository.updateStudent(any())).thenReturn(student);
    	
    	Student updated = studentService.updateProfile(student.getStudentId(),newName, newEmail);
    	
    	Assertions.assertEquals(newEmail, updated.getEmail(),"Email Mismatch after Update");
    	Assertions.assertEquals(newName, updated.getName(),"Name Mismatch after Update");
    	
    }

    @Test
    void testUpdateProfileWithDuplicateEmailThrowsException() {
    	String email = "dupemailupdateprofiletest@example.com";
    	
    	when(studentRepository.getStudentByEmail(email)).thenReturn(new Student());
    	
    	Assertions.assertThrows(EmailAlreadyExistsException.class, ()->{
    		studentService.updateProfile("Id", "name", email);
    	});
    	
    }

    // ----- utility method

    private Student getStudent(String email , String name) {
    	String studentId = "Test User - "+ UUID.randomUUID().toString();
    	
    	Student student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setStudentId(studentId);
    	student.setPasswordHash(password);
    	
    	return student;
    }
//    
    private Student getStudent(String email) {
    	String name = "Test User StudentServiceTest";
    	return getStudent(email,name);
    }
}
