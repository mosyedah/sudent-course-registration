package enrollment.courseenrollment.webApp;

import enrollment.courseenrollment.service.StudentService;
import enrollment.courseenrollment.exceptions.EmailAlreadyExistsException;
import enrollment.courseenrollment.exceptions.InvalidCredentialsException;
import enrollment.courseenrollment.exceptions.StudentNotFoundException;
import enrollment.courseenrollment.model.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    private final StudentService studentService;

    public WebController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ----------------- HOME URL -----------------
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId != null) {
            return "redirect:/dashboard"; // already logged in
        }
        return "home"; // show login + signup options
    }

    // ----------------- LOGIN -----------------
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        
        try {
        	Student student = studentService.login(email, password);
        	session.setAttribute("studentId", student.getStudentId());
        	return "redirect:/dashboard";
			
		} catch (StudentNotFoundException | InvalidCredentialsException e) {
			model.addAttribute("loginError", "Invalid email or password");
			return "home"; // reload home page with error
		}
    }

    // ----------------- SIGNUP -----------------
    @PostMapping("/signup")
    public String processSignup(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
    	Student student = new Student();
    	student.setEmail(email);
    	student.setName(name);
    	student.setPasswordHash(password);
    	try {
    		student = studentService.signUp(student); 
    		session.setAttribute("studentId", student.getStudentId());
    		return "redirect:/dashboard";
			
		} catch (EmailAlreadyExistsException e) {
			model.addAttribute("signupError", "Email Already Registered");
			return "home"; 
			
		}
    }


    // ----------------- LOGOUT -----------------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; // back to home/login
    }
}

