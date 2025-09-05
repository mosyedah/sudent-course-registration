package enrollment.courseenrollment.repository;

import java.util.List;

import enrollment.courseenrollment.model.Course;

public interface CourseRepository {
	Course getCourseById(String courseId);
	List<Course> getAllCourses();
}
