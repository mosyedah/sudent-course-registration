package enrollment.courseEnrollment.service;

import enrollment.courseenrollment.repository.CourseRepository;
import enrollment.courseenrollment.repository.EnrollmentRepository;
import enrollment.courseenrollment.service.CourseService;
import enrollment.courseenrollment.service.LogService;
import enrollment.courseenrollment.service.WaitlistService;
import enrollment.courseenrollment.exceptions.CourseAlreadyAppliedException;
import enrollment.courseenrollment.exceptions.CourseNotFoundException;
import enrollment.courseenrollment.exceptions.DropNotAllowedAfterCourseEndDateException;
import enrollment.courseenrollment.exceptions.DropNotAllowedForEnrollmentStatusException;
import enrollment.courseenrollment.exceptions.MaxEnrollmentsLimitReachedException;
import enrollment.courseenrollment.exceptions.MaxWaitlistedLimitReachedException;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

   
    
	 	@Mock
	    private CourseRepository courseRepository;

	    @Mock
	    private EnrollmentRepository enrollmentRepository;

	    @Mock
	    private LogService logService;

	    // build a real WaitlistService using mocks
	    private WaitlistService waitlistService;

	    // build a real CourseService using mocks + real waitlistService
	    private CourseService courseService;
	    

	    @BeforeEach
	    void setUp() {
	        waitlistService = new WaitlistService(enrollmentRepository, logService, courseRepository);
	        courseService = new CourseService(courseRepository, enrollmentRepository, logService, waitlistService);
	    }
    
    
    // =========================
    // Basic Course Retrieval Tests
    // =========================

    @Test
    void shouldReturnCourse_whenCourseIdExists() {
        // TODO: Mock repository to return a Course for a given courseId
        // Call courseService.getCourseById(courseId)
        // Assert returned course matches expected
    	
    	Course dummyCourse = new Course();
        String courseId = "CS101";
        dummyCourse.setCourseId(courseId);

        when(courseRepository.getCourseById(courseId)).thenReturn(dummyCourse);

        Course result = courseService.getCourseById(courseId);

        assertNotNull(result,"Received null for valid Course ID");
        assertEquals(courseId, result.getCourseId(), "Course ID Mismatch");
        assertTrue(dummyCourse==result, "Diferent reference of objects");

        // interesting method, checks how many times a method was called
        verify(courseRepository, times(1)).getCourseById(courseId);
        
    }

    @Test
    void shouldThrowException_whenCourseIdDoesNotExist() {
        // TODO: Mock repository to return null
        // Call courseService.getCourseById(courseId)
        // Assert CourseNotFoundException is thrown
    	String nonExistingCourseId = "CSXXX";
    	
    	when(courseRepository.getCourseById(nonExistingCourseId)).thenReturn(null);
    	
    	assertThrows(CourseNotFoundException.class, ()->{
    		courseService.getCourseById(nonExistingCourseId);
    	}, "Exception was not thrown for invalid course ID");
    }

    @Test
    void shouldReturnAllCourses_whenCoursesExist() {
        // TODO: Mock repository to return a list of courses
        // Call courseService.viewAllCourses()
        // Assert returned list matches expected
    	String courseId = "CS101";
    	Course course = new Course();
    	course.setCourseId(courseId);
    	
    	List<Course> courses = new ArrayList<Course>();
    	courses.add(course);
    	
    	when(courseRepository.getAllCourses()).thenReturn(courses);
    	
    	List<Course> returned = courseService.viewAllCourses();
    	
    	assertNotNull(returned, "Got Null when expect list of courses");
    	
    	assertTrue(courses==returned,"Not the same list");
    	
    	assertEquals(courseId, returned.get(0).getCourseId(),"Mismatch Course Id");
    	
    	assertEquals(1, returned.size(), " size of list changed");
    	

    }

    
    
   

    // =========================
    // Enrollment Constraints Tests
    // =========================

    @Test
    void shouldThrowException_whenStudentEnrollsInvalidCourseId() {
    	String nonExistingCourseId = "CSXXX";
    	String studentId = "SampleId";
    	
    	when(courseRepository.getCourseById(nonExistingCourseId)).thenReturn(null);
    	
    	assertThrows(CourseNotFoundException.class, ()->{
    		courseService.enroll(studentId, nonExistingCourseId);
    	}, "Exception was not thrown for invalid course ID");
    	
    }
    
    @Test
    @DisplayName("Test for course already applied")
    void shouldThrowException_whenStudentTriesDoubleEnroll() {
    	Enrollment enrollment = new Enrollment();
    	enrollment.setStatus(EnrollmentStatus.ENROLLED);
    	
    	Course course = new Course();
    	course.setLatestEnrollmentBy(Instant.now().plus(5, ChronoUnit.DAYS));
    	
    	when(courseRepository.getCourseById(anyString())).thenReturn(course);
    	
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(anyString(),anyString())).thenReturn(enrollment);
    
    	
    	assertThrows(CourseAlreadyAppliedException.class, ()->{
    		courseService.enroll("ss", "ss");    		
    	}, "Should throw CourseAlreadyAppliedException, when status is other than opt Out");
    }
    
    
    @Test
    void shouldThrowException_whenStudentEnrollsMoreThan5Courses() {
        // TODO: Mock repository to return 5 ENROLLED courses for student
        // Call courseService.enroll(...)
        // Assert exception is thrown
    	String studentId = "studentId";
    	String courseId = "CS101";
    	Instant enrollmentBy = Instant.now().plus(5,ChronoUnit.DAYS);
    	Course course = new Course();
    	int maxLimitEnrollCourse = 5;
    	course.setCourseId(courseId);
    	course.setLatestEnrollmentBy(enrollmentBy);
    	course.setMaxSeats(10);
    	
    	
    	when(courseRepository.getCourseById(courseId)).thenReturn(course);
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(studentId, courseId)).thenReturn(null);
    	when(enrollmentRepository.getWaitlistedEnrollmentsByCourseId(courseId)).thenReturn(new ArrayList<Enrollment>());
    	when(enrollmentRepository.getEnrollmentCountByStudentIdAndStatus(studentId, EnrollmentStatus.ENROLLED)).thenReturn(maxLimitEnrollCourse);
    	
    	
    	assertThrows(MaxEnrollmentsLimitReachedException.class, ()->{
    		courseService.enroll(studentId, courseId);
    	}, "Max Enrollment Limit Exception was not thrown when Max limit is reached");
    	
    }

    @Test
    void shouldThrowException_whenStudentWaitlistsMoreThan3Courses() {
    	// TODO: Mock repository to return 3 WAITLISTED courses for student
    	// Call courseService.enroll(...)
    	// Assert exception is thrown
    	String studentId = "studentId";
    	String courseId = "CS101";
    	Instant enrollmentBy = Instant.now().plus(5,ChronoUnit.DAYS);
    	Course course = new Course();
    	int maxLimitWaitlistCourse = 3;
    	course.setCourseId(courseId);
    	course.setLatestEnrollmentBy(enrollmentBy);
    	course.setMaxSeats(0);
    	
    	
    	when(courseRepository.getCourseById(courseId)).thenReturn(course);
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(studentId, courseId)).thenReturn(null);
    	when(enrollmentRepository.getWaitlistedEnrollmentsByCourseId(courseId)).thenReturn(new ArrayList<Enrollment>());
    	when(enrollmentRepository.getEnrollmentCountByStudentIdAndStatus(studentId, EnrollmentStatus.WAITLISTED)).thenReturn(maxLimitWaitlistCourse);
    	
    	
    	assertThrows(MaxWaitlistedLimitReachedException.class, ()->{
    		courseService.enroll(studentId, courseId);
    	}, "Max wailist Limit Exception was not thrown when Max limit is reached");
    }

    // =========================
    // Drop Method Rules - Invalid Cases
    // =========================

    @Test
    void shouldThrowException_whenDroppingCourseAfterEndDate() {
        // TODO: Mock course with endDate < now
        // Call courseService.drop(...)
        // Assert DropNotAllowedAfterCourseEndDateException is thrown
    	String studentId = "studentId";
    	String courseId = "CS101";
    	Instant endDate = Instant.now().minus(5,ChronoUnit.DAYS);
    	Course course = new Course();
    	course.setCourseId(courseId);
    	course.setEndDate(endDate);
    	
    	when(courseRepository.getCourseById(courseId)).thenReturn(course);
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(studentId, courseId)).thenReturn(new Enrollment());
    	
    	assertThrows(DropNotAllowedAfterCourseEndDateException.class, ()->{
    		courseService.drop(studentId, courseId);
    	},"Drop SHould not be allowed after End date");
    	
    	
    	
    }

    @Test
    void shouldThrowException_whenDroppingCourseWithInvalidStatus() {
        // TODO: Mock enrollment with status other than ENROLLED or WAITLISTED
        // Call courseService.drop(...)
        // Assert DropNotAllowedForEnrollmentStatusException is thrown
    	String studentId = "studentId";
    	String courseId = "CS101";
    	Instant endDate = Instant.now().plus(5,ChronoUnit.DAYS);
    	Course course = new Course();
    	course.setCourseId(courseId);
    	course.setEndDate(endDate);
    	
    	Enrollment enrollment = new Enrollment();
    	enrollment.setStatus(EnrollmentStatus.OPTED_OUT);
    	
    	when(courseRepository.getCourseById(courseId)).thenReturn(course);
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(studentId, courseId)).thenReturn(enrollment);
    	
    	assertThrows(DropNotAllowedForEnrollmentStatusException.class, ()->{
    		courseService.drop(studentId, courseId);
    	},"Drop SHould not be allowed For Status other than ENrolled,Waitlist");
    }

    // =========================
    // Drop Method Rules - Enrolled Course Cases
    // =========================

    @Test
    void shouldWithdrawEnrollmentAndReleaseSeat_whenDroppingBeforeEnrollmentByDate() {
        // TODO: Mock course ENROLLED with latestEnrollmentBy > now
        // Drop should -> status = WITHDRAWN, seat released
        // If waitlisted students exist -> first becomes ENROLLED, others shift positions
    }

    @Test
    void shouldDropEnrollmentAndNotReleaseSeat_whenDroppingAfterEnrollmentByButBeforeEndDate() {
        // TODO: Mock course ENROLLED with latestEnrollmentBy < now < endDate
        // Drop should -> status = DROPPED, seat not released
    }

    // =========================
    // Drop Method Rules - Waitlist Cases
    // =========================

    @Test
    void shouldOptOutWaitlistAndAdjustRemainingPositions_whenDroppingWaitlistedCourse() {
        // TODO: Mock enrollment WAITLISTED
        // Drop should -> status = OPTED_OUT
        // Adjust positions of remaining waitlisted students
    	
    	String courseId  = "CS101";
    	String studentId = "StudentId";
    	Course course = new Course();
    	course.setEndDate(Instant.now().plus(5, ChronoUnit.DAYS));
    	
    	Enrollment enrollment1 = new Enrollment();
    	Enrollment enrollment2 = new Enrollment();
    	Enrollment enrollment3 = new Enrollment();
    	Enrollment enrollment4 = new Enrollment();
    	
    	enrollment1.setStatus(EnrollmentStatus.WAITLISTED);
    	enrollment2.setStatus(EnrollmentStatus.WAITLISTED);
    	enrollment3.setStatus(EnrollmentStatus.WAITLISTED);
    	enrollment4.setStatus(EnrollmentStatus.WAITLISTED);
    	
    	enrollment1.setPositionInWaitlist(4);
    	enrollment2.setPositionInWaitlist(2);
    	enrollment3.setPositionInWaitlist(1);
    	enrollment4.setPositionInWaitlist(3);
    	
    	List<Enrollment> enrollments = new ArrayList<Enrollment>();
    	enrollments.add(enrollment1);
    	enrollments.add(enrollment2);
    	enrollments.add(enrollment3);
    	enrollments.add(enrollment4);
    	
    	when(courseRepository.getCourseById(courseId)).thenReturn(course);
    	
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(studentId, courseId)).thenReturn(enrollment2);
    	
    	List<Enrollment> modifiedEnrollments = new ArrayList<Enrollment>(enrollments);
    	modifiedEnrollments.remove(enrollment2);
    	
    	when(enrollmentRepository.getWaitlistedEnrollmentsByCourseId(courseId)).thenReturn(modifiedEnrollments);
    	
    	when(enrollmentRepository.updateEnrollment(any())).thenReturn(true);
    	
    	boolean result =  courseService.drop(studentId, courseId);
    	
    	assertTrue(result);

    	assertEquals(EnrollmentStatus.OPTED_OUT, enrollment2.getStatus(),
    			"Drop status not updated from Waitlisted to Opted Out");
    	
    	try {
			Thread.sleep(200); // this is just so that async waitlist adjust is done ,else they are failing sometimes
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	assertEquals(3, enrollment1.getPositionInWaitlist(),"Waitlist Adjustment not correct");
    	assertEquals(1, enrollment3.getPositionInWaitlist(),"Waitlist Adjustment not correct");
    	assertEquals(2, enrollment4.getPositionInWaitlist(),"Waitlist Adjustment not correct");
    	
    }

    // =========================
    // Waitlist Adjustment Tests
    // =========================

    @Test
    void shouldEnrollFirstWaitlistedStudent_whenSeatReleased() {
        // TODO: Mock full course with ENROLLED + WAITLISTED students
        // Drop one ENROLLED student
        // First waitlisted student should become ENROLLED
    	String courseId  = "CS101";
    	String studentId = "StudentId";
    	Course course = new Course();
    	course.setEndDate(Instant.now().plus(25, ChronoUnit.DAYS));
    	course.setLatestEnrollmentBy(Instant.now().plus(5, ChronoUnit.DAYS));
    	course.setMaxSeats(10);
    	course.setSeatsFilled(9);
    	
    	Enrollment enrollment1 = new Enrollment();
    	Enrollment enrollment2 = new Enrollment();
    	Enrollment enrollment3 = new Enrollment();
    	Enrollment enrollment4 = new Enrollment();
    	Enrollment enrollment5 = new Enrollment();
    	
    	enrollment5.setStatus(EnrollmentStatus.ENROLLED); // should be withdrawn
    	enrollment2.setStatus(EnrollmentStatus.WAITLISTED);
    	enrollment3.setStatus(EnrollmentStatus.WAITLISTED);
    	enrollment4.setStatus(EnrollmentStatus.WAITLISTED); // should be enrolled
    	enrollment1.setStatus(EnrollmentStatus.WAITLISTED);
    	
    	enrollment1.setPositionInWaitlist(4);
    	enrollment2.setPositionInWaitlist(3);
    	enrollment3.setPositionInWaitlist(2);
    	enrollment4.setPositionInWaitlist(1);
    	
    	List<Enrollment> enrollments = new ArrayList<Enrollment>();
    	enrollments.add(enrollment1);
    	enrollments.add(enrollment2);
    	enrollments.add(enrollment3);
    	enrollments.add(enrollment4);
    	
    	when(courseRepository.getCourseById(courseId)).thenReturn(course);
    	
    	when(enrollmentRepository.getEnrollmentByStudentAndCourse(studentId, courseId)).thenReturn(enrollment5);
    	
    	List<Enrollment> modifiedEnrollments = new ArrayList<Enrollment>(enrollments);
    	modifiedEnrollments.remove(enrollment5);
    	
    	when(enrollmentRepository.getWaitlistedEnrollmentsByCourseId(courseId)).thenReturn(modifiedEnrollments);
    	
    	when(enrollmentRepository.updateEnrollment(any())).thenReturn(true);
    	
    	boolean result =  courseService.drop(studentId, courseId);
    	
    	assertTrue(result);
    	
    	assertEquals(EnrollmentStatus.WITHDRAWN, enrollment5.getStatus(),
    			"Drop status not updated from Enrolled to Withdrawn");
    	
    	
    	try {
    		Thread.sleep(200); // this is just so that async waitlist adjust is done ,else they are failing sometimes
    	} catch (Exception e) {
    		// TODO: handle exception
    	}
    	
    	assertEquals(EnrollmentStatus.ENROLLED, enrollment4.getStatus(),
    			"Drop status not updated from Waitlisted to Enrolled");
    	
    	assertEquals(3, enrollment1.getPositionInWaitlist(),"Waitlist Adjustment not correct");
    	assertEquals(1, enrollment3.getPositionInWaitlist(),"Waitlist Adjustment not correct");
    	assertEquals(2, enrollment2.getPositionInWaitlist(),"Waitlist Adjustment not correct");
    	
    }

   

    
}
