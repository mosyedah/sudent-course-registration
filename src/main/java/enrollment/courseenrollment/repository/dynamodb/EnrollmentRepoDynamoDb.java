package enrollment.courseenrollment.repository.dynamodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enrollment.courseenrollment.exceptions.DatabaseUnknownException;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;
import enrollment.courseenrollment.repository.EnrollmentRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.EnrollementTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class EnrollmentRepoDynamoDb implements EnrollmentRepository{
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	
	@Override
	public boolean createEnrollment(Enrollment enrollment) {
		// TODO Auto-generated method stub
		Map<String, AttributeValue>item = new HashMap<String, AttributeValue>();
		
		item.put(EnrollementTableConstants.STUDENT_ID, AttributeValue.builder().s(enrollment.getStudentId()).build());
		item.put(EnrollementTableConstants.COURSE_ID, AttributeValue.builder().s(enrollment.getCourseId()).build());
		item.put(EnrollementTableConstants.STATUS, AttributeValue.builder().s(enrollment.getStatus().toString()).build());
		
		if (enrollment.getPositionInWaitlist()!=null)
			item.put(EnrollementTableConstants.POSITION_WAITLIST, 
					AttributeValue.builder().n(enrollment.getPositionInWaitlist().toString()).build());
		if (enrollment.getWaitlistedAt()!=null)
			item.put(EnrollementTableConstants.WAITLISTED_AT, 
					AttributeValue.builder().s(enrollment.getWaitlistedAt().toString()).build());
		if (enrollment.getWaitlistedAt()!=null)
			item.put(EnrollementTableConstants.ENROLLED_AT, 
					AttributeValue.builder().s(enrollment.getEnrolledAt().toString()).build());
		PutItemRequest request = PutItemRequest.builder()
				.tableName(EnrollementTableConstants.TABLE_NAME).item(item).build();
		
		try {
			client.putItem(request);
			return true;
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error, Try Later");
		}
	}

	@Override
	public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
		// TODO Auto-generated method stub
		QueryRequest request = QueryRequest.builder()
				.tableName(EnrollementTableConstants.TABLE_NAME)
				.keyConditionExpression(EnrollementTableConstants.STUDENT_ID+"= :studentId")
				.expressionAttributeValues(Map.of(":studentId",AttributeValue.builder().s(studentId).build()))
				.build();
		List<Enrollment> enrollments = new ArrayList<Enrollment>();	
		try {
			QueryResponse response = client.query(request);
			for (Map<String, AttributeValue> item : response.items()) {
				enrollments.add(itemToEnrollment(item));
			}
			return enrollments;
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error, Try Later");
		}
	}

	@Override
	public List<Enrollment> getWaitlistedEnrollmentsByCourseId(String courseId) {
		// TODO Auto-generated method stub
		QueryRequest request = QueryRequest.builder()
				.tableName(EnrollementTableConstants.TABLE_NAME)
				.indexName(EnrollementTableConstants.COURSE_INDEX)
				.keyConditionExpression(EnrollementTableConstants.COURSE_ID+" = :courseId")
				.filterExpression(EnrollementTableConstants.STATUS+" = :status")
				.expressionAttributeValues(Map.of(
						":courseId" , AttributeValue.builder().s(courseId).build(),
						":status",AttributeValue.builder().s(EnrollmentStatus.WAITLISTED.toString()).build() 
						))
				.build();
		return null;
	}

	@Override
	public boolean updateEnrollment(Enrollment enrollment) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteEnrollment(String enrollmentId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Enrollment itemToEnrollment(Map<String, AttributeValue> item) {
	    Enrollment enrollment = new Enrollment();

	   // always present
	    enrollment.setStudentId(item.get(EnrollementTableConstants.STUDENT_ID).s());
	    enrollment.setCourseId(item.get(EnrollementTableConstants.COURSE_ID).s());
	    enrollment.setStatus(item.get(EnrollementTableConstants.STATUS).s());

	    // Optional Fields
	    AttributeValue positionAttr = item.get(EnrollementTableConstants.POSITION_WAITLIST);
	    if (positionAttr != null && positionAttr.n() != null) {
	        enrollment.setPositionInWaitlist(Integer.parseInt(positionAttr.n()));
	    }

	    AttributeValue waitlistedAtAttr = item.get(EnrollementTableConstants.WAITLISTED_AT);
	    if (waitlistedAtAttr != null && waitlistedAtAttr.s() != null) {
	        enrollment.setWaitlistedAt(waitlistedAtAttr.s());
	    }

	    AttributeValue enrolledAtAttr = item.get(EnrollementTableConstants.ENROLLED_AT);
	    if (enrolledAtAttr != null && enrolledAtAttr.s() != null) {
	        enrollment.setEnrolledAt(enrolledAtAttr.s());
	    }

	    AttributeValue droppedAtAttr = item.get(EnrollementTableConstants.DROPPED_AT);
	    if (droppedAtAttr != null && droppedAtAttr.s() != null) {
	        enrollment.setDroppedAt(droppedAtAttr.s());
	    }

	    AttributeValue optedOutAtAttr = item.get(EnrollementTableConstants.OPTED_OUT_AT);
	    if (optedOutAtAttr != null && optedOutAtAttr.s() != null) {
	        enrollment.setOptedOutAt(optedOutAtAttr.s());
	    }

	    return enrollment;
	}


}
