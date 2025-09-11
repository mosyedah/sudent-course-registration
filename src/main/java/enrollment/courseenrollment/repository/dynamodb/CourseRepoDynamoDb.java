package enrollment.courseenrollment.repository.dynamodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import enrollment.courseenrollment.exceptions.CourseNotFoundException;
import enrollment.courseenrollment.exceptions.DatabaseUnknownException;
import enrollment.courseenrollment.model.Course;
import enrollment.courseenrollment.repository.CourseRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.CourseTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

public class CourseRepoDynamoDb implements CourseRepository{
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	@Override
	public Course getCourseById(String courseId) {
		// TODO Auto-generated method stub
		GetItemRequest  request = GetItemRequest.builder()
				.tableName(CourseTableConstants.TABLE_NAME)
				.key(Map.of(
						CourseTableConstants.COURSE_ID,AttributeValue.builder().s(courseId).build()))
				.build();
		try {
			GetItemResponse response = client.getItem(request);
			if (!response.hasItem()) 
				throw new CourseNotFoundException("Course not found for CourseId : "+ courseId);
			Map<String, AttributeValue> item = response.item();
			return itemToCourse(item);
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown error, try later");
		}
	}

	@Override
	public List<Course> getAllCourses() {
		// TODO Auto-generated method stub
		ScanRequest request = ScanRequest.builder()
				.tableName(CourseTableConstants.TABLE_NAME)
				.build();
		List<Course> courses = new ArrayList<Course>();
		try {			
			ScanResponse response = client.scan(request);
			for (Map<String, AttributeValue> item : response.items()) {
				courses.add(itemToCourse(item));
			}
			return courses;
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error, Try Later");
		}
	}
	
	private Course itemToCourse(Map<String, AttributeValue> item) {
		Course course = new Course();
		course.setCourseId(item.get(CourseTableConstants.COURSE_ID).s());
		course.setCourseName(item.get(CourseTableConstants.COURSE_NAME).s());
		course.setMaxSeats(Integer.parseInt(item.get(CourseTableConstants.MAX_SEATS).n()));
		course.setSeatsFilled(Integer.parseInt(item.get(CourseTableConstants.SEATS_FILLED).n()));
		course.setStartDate(Instant.parse(item.get(CourseTableConstants.START_DATE).s()));
		course.setEndDate(Instant.parse(item.get(CourseTableConstants.END_DATE).s()));
		course.setLatestEnrollmentBy(Instant.parse(item.get(CourseTableConstants.ENROLL_BY_DATE).s()));
		
		return course;
		
	}

}
