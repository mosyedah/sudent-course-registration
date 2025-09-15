package enrollment.courseenrollment.webApp;

import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.exceptions.CourseAlreadyAppliedException;
import enrollment.courseenrollment.exceptions.CourseEnrollmentDateHasPassedException;
import enrollment.courseenrollment.exceptions.CourseNotFoundException;
import enrollment.courseenrollment.exceptions.DropNotAllowedAfterCourseEndDateException;
import enrollment.courseenrollment.exceptions.DropNotAllowedForEnrollmentStatusException;
import enrollment.courseenrollment.exceptions.EmailAlreadyExistsException;
import enrollment.courseenrollment.exceptions.MaxEnrollmentsLimitReachedException;
import enrollment.courseenrollment.exceptions.MaxWaitlistedLimitReachedException;
import enrollment.courseenrollment.exceptions.StudentNotEnrolledForThisCourseException;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.service.StudentService;
import enrollment.courseenrollment.service.CourseService;
import enrollment.courseenrollment.service.WaitlistService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final WaitlistService waitlistService;

    public DashboardController(StudentService studentService,
                               CourseService courseService,
                               WaitlistService waitlistService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.waitlistService = waitlistService;
    }

    // ----------------- DASHBOARD PAGE -----------------
    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId == null) {
            return "redirect:/"; // session invalid, back to home
        }

        populate(model, studentId);

        return "dashboard"; // loads dashboard.html
    }

    // ----------------- EDIT PROFILE -----------------
    @PostMapping("/edit-profile")
    public String editProfile(@RequestParam String name,
                              @RequestParam String email,
                              HttpSession session,
                              Model model) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/";
        try {
        	Student updated = studentService.updateProfile(studentId, name, email);
        	model.addAttribute("successMessage", "Profile updated successfully");
			
		} catch (EmailAlreadyExistsException e) {
			model.addAttribute("errorMessage",e.getMessage());
		}
        populate(model, studentId);
        return "dashboard";
    }

    // ----------------- CHANGE PASSWORD -----------------
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session,
                                 Model model) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/";

        boolean success = studentService.changePassword(studentId, newPassword);
        if (success) {
            model.addAttribute("successMessage", "Password updated successfully");
        } else {
            model.addAttribute("errorMessage", "Invalid current password");
        }
        populate(model, studentId);
        return "dashboard";
    }

    // ----------------- ENROLL IN COURSE -----------------
    @PostMapping("/enroll")
    public String enrollCourse(@RequestParam String courseId,
                               HttpSession session,
                               Model model) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/";
        try {
        	courseService.enroll(studentId, courseId);
        	model.addAttribute("successMessage", "Successfully Applied for course");
        	
		} catch (CourseNotFoundException | 
				CourseAlreadyAppliedException |
				CourseEnrollmentDateHasPassedException |
				MaxWaitlistedLimitReachedException |
				MaxEnrollmentsLimitReachedException e) {
			model.addAttribute("errorMessage",e.getMessage());
		}
        populate(model, studentId);
        return "dashboard"; // reload dashboard to reflect changes
    }

    // ----------------- DROP COURSE -----------------
    @PostMapping("/drop")
    public String dropCourse(@RequestParam String courseId,
                             HttpSession session,
                             Model model) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId == null) return "redirect:/";
        try {
        	courseService.drop(studentId, courseId);
        	model.addAttribute("successMessage", "Successfully Dropped for course");
        	
        } catch (CourseNotFoundException | 
        		StudentNotEnrolledForThisCourseException |
        		DropNotAllowedAfterCourseEndDateException |
        		DropNotAllowedForEnrollmentStatusException e) {
        	model.addAttribute("errorMessage",e.getMessage());
        }
        populate(model, studentId);
        return "dashboard"; // reload dashboard to reflect changes
    }
    
    private void populate(Model model, String studentId) {
    	// Fetch profile details
        Student student = studentService.getStudentbyId(studentId);
        model.addAttribute("student", student);

        // Fetch all available courses
        List<Course> availableCourses = courseService.viewAllCourses();
        model.addAttribute("availableCourses", availableCourses);

        // Fetch student's enrolled + waitlisted courses
        List<Enrollment> myCourses = courseService.getEnrollmentsByStudentId(studentId);
        model.addAttribute("myCourses", myCourses);
        
        //  courseById map
        Map<String, Course> courseById = availableCourses.stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        model.addAttribute("courseById", courseById);
    }
}
