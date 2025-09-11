package enrollment.courseenrollment.repository.dynamodb;

import java.util.ArrayList;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enrollment.courseenrollment.exceptions.DatabaseUnknownException;
import enrollment.courseenrollment.model.Enrollment;
import enrollment.courseenrollment.model.enums.EnrollmentStatus;
import enrollment.courseenrollment.repository.EnrollmentRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.CourseTableConstants;
import enrollment.courseenrollment.repository.dynamodb.constants.EnrollmentTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class EnrollmentRepoDynamoDb implements EnrollmentRepository{
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	
	@Override
	public boolean createEnrollment(Enrollment enrollment) {
		// TODO Auto-generated method stub
		Map<String, AttributeValue>item = new HashMap<String, AttributeValue>();
		
		item.put(EnrollmentTableConstants.STUDENT_ID, AttributeValue.builder().s(enrollment.getStudentId()).build());
		item.put(EnrollmentTableConstants.COURSE_ID, AttributeValue.builder().s(enrollment.getCourseId()).build());
		item.put(EnrollmentTableConstants.STATUS, AttributeValue.builder().s(enrollment.getStatus().toString()).build());
		
		if (enrollment.getPositionInWaitlist()!=null)
			item.put(EnrollmentTableConstants.POSITION_WAITLIST, 
					AttributeValue.builder().n(enrollment.getPositionInWaitlist().toString()).build());
		if (enrollment.getWaitlistedAt()!=null)
			item.put(EnrollmentTableConstants.WAITLISTED_AT, 
					AttributeValue.builder().s(enrollment.getWaitlistedAt().toString()).build());
		if (enrollment.getEnrolledAt()!=null)
			item.put(EnrollmentTableConstants.ENROLLED_AT, 
					AttributeValue.builder().s(enrollment.getEnrolledAt().toString()).build());
		if (enrollment.getDroppedAt()!=null)
			item.put(EnrollmentTableConstants.DROPPED_AT, 
					AttributeValue.builder().s(enrollment.getDroppedAt().toString()).build());
		if (enrollment.getOptedOutAt()!=null)
			item.put(EnrollmentTableConstants.OPTED_OUT_AT, 
					AttributeValue.builder().s(enrollment.getOptedOutAt().toString()).build());
		
		 try {
		        if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
		            // Build Put request for enrollment
		            Put enrollmentPut = Put.builder()
		                    .tableName(EnrollmentTableConstants.TABLE_NAME)
		                    .item(item)
		                    .build();

		            // Build Update request for course seatsFilled
		            Update courseUpdate = Update.builder()
		                    .tableName(CourseTableConstants.TABLE_NAME)
		                    .key(Map.of(
		                            CourseTableConstants.COURSE_ID,
		                            AttributeValue.builder().s(enrollment.getCourseId()).build()
		                    ))
		                    .updateExpression("ADD #seats :inc")
		                    .expressionAttributeValues(Map.of(
		                            ":inc", AttributeValue.builder().n("1").build()
		                    ))
		                    .conditionExpression("#seats < #maxSeats") // to check at enroll time as well to be atomic
		                    .expressionAttributeNames(Map.of(
		                    		"#seats" , CourseTableConstants.SEATS_FILLED,
		                    		"#maxSeats", CourseTableConstants.MAX_SEATS
		                    		))
		                    .build();

		            // Build transaction request
		            TransactWriteItemsRequest transactRequest = TransactWriteItemsRequest.builder()
		                    .transactItems(
		                            TransactWriteItem.builder().put(enrollmentPut).build(),
		                            TransactWriteItem.builder().update(courseUpdate).build()
		                    )
		                    .build();

		            client.transactWriteItems(transactRequest);

		        } else {
		            // Not ENROLLED -> just put enrollment
		            PutItemRequest putRequest = PutItemRequest.builder()
		                    .tableName(EnrollmentTableConstants.TABLE_NAME)
		                    .item(item)
		                    .build();
		            client.putItem(putRequest);
		        }

		        return true;
		    } 
		 	catch (TransactionCanceledException e) {
				return false;
			}
		 	catch (Exception e) {
		        throw new DatabaseUnknownException("Unknown Error, Try Later");
		    }
	}
	

	@Override
	public Enrollment getEnrollmentByStudentAndCourse(String studentId, String courseId) {
		GetItemRequest request = GetItemRequest.builder()
				.tableName(EnrollmentTableConstants.TABLE_NAME)
				.key(Map.of(
						EnrollmentTableConstants.STUDENT_ID, AttributeValue.builder().s(studentId).build(),
						EnrollmentTableConstants.COURSE_ID,AttributeValue.builder().s(courseId).build()
						))
				.build();
		try {
			GetItemResponse response = client.getItem(request);
			if(!response.hasItem()) 
				return null;
			return itemToEnrollment(response.item());
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error , Try Later");
		}
	}

	@Override
	public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
		// TODO Auto-generated method stub
		QueryRequest request = QueryRequest.builder()
				.tableName(EnrollmentTableConstants.TABLE_NAME)
				.keyConditionExpression("#studentId = :studentId")
				.expressionAttributeValues(Map.of(":studentId",AttributeValue.builder().s(studentId).build()))
				.expressionAttributeNames(Map.of(
						"#studentId", EnrollmentTableConstants.STUDENT_ID
						))
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
				.tableName(EnrollmentTableConstants.TABLE_NAME)
				.indexName(EnrollmentTableConstants.COURSE_INDEX)
				.keyConditionExpression("#courseId = :courseId")
				.filterExpression("#status = :status")
				.expressionAttributeValues(Map.of(
						":courseId" , AttributeValue.builder().s(courseId).build(),
						":status",AttributeValue.builder().s(EnrollmentStatus.WAITLISTED.toString()).build() 
						))
				.expressionAttributeNames(Map.of(
						"#courseId",EnrollmentTableConstants.COURSE_ID,
						"#status",EnrollmentTableConstants.STATUS
						))
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
	public boolean updateEnrollment(Enrollment enrollment) {
	    Map<String, AttributeValue> key = Map.of(
	            EnrollmentTableConstants.STUDENT_ID,
	            AttributeValue.builder().s(enrollment.getStudentId()).build(),
	            EnrollmentTableConstants.COURSE_ID,
	            AttributeValue.builder().s(enrollment.getCourseId()).build()
	    );

	    StringBuilder updateExpression = new StringBuilder("SET ");
	    Map<String, AttributeValue> expressionValues = new HashMap<>();
	    Map<String, String> expressionNames = new HashMap<>();

	    appendUpdateField(updateExpression, expressionValues, expressionNames,
	            EnrollmentTableConstants.STATUS,
	            enrollment.getStatus() != null ? AttributeValue.builder().s(enrollment.getStatus().name()).build() : null);

	    appendUpdateField(updateExpression, expressionValues,expressionNames,
	            EnrollmentTableConstants.POSITION_WAITLIST,
	            enrollment.getPositionInWaitlist() != null ? AttributeValue.builder().n(enrollment.getPositionInWaitlist().toString()).build() : null);

	    appendUpdateField(updateExpression, expressionValues,expressionNames,
	            EnrollmentTableConstants.WAITLISTED_AT,
	            enrollment.getWaitlistedAt() != null ? AttributeValue.builder().s(enrollment.getWaitlistedAt().toString()).build() : null);

	    appendUpdateField(updateExpression, expressionValues,expressionNames,
	            EnrollmentTableConstants.ENROLLED_AT,
	            enrollment.getEnrolledAt() != null ? AttributeValue.builder().s(enrollment.getEnrolledAt().toString()).build() : null);

	    appendUpdateField(updateExpression, expressionValues, expressionNames,
	            EnrollmentTableConstants.DROPPED_AT,
	            enrollment.getDroppedAt() != null ? AttributeValue.builder().s(enrollment.getDroppedAt().toString()).build() : null);

	    appendUpdateField(updateExpression, expressionValues,expressionNames,
	            EnrollmentTableConstants.OPTED_OUT_AT,
	            enrollment.getOptedOutAt() != null ? AttributeValue.builder().s(enrollment.getOptedOutAt().toString()).build() : null);

	    if (expressionValues.isEmpty()) return false;
	    
	   

	    try {
	    	EnrollmentStatus status = enrollment.getStatus();
	    	if (EnrollmentStatus.DROPPED == status || EnrollmentStatus.ENROLLED == status) {
	    		// Enrollment Update
				Update enrollmentUpdate = Update.builder()
						.tableName(EnrollmentTableConstants.TABLE_NAME)
						.key(key)
						.updateExpression(updateExpression.toString())
						.expressionAttributeValues(expressionValues)
						.expressionAttributeNames(expressionNames)
						.build();
				String conditionExpression;
				Map<String, AttributeValue> courseExpressionValues = new HashMap<String, AttributeValue>();
				Map<String, String> courseExpressionNames = new HashMap<String, String>();
				if (status == EnrollmentStatus.DROPPED ) {
					conditionExpression = "#seats > :zero";
					courseExpressionNames.put("#seats", CourseTableConstants.SEATS_FILLED);
					courseExpressionValues.put(":zero", AttributeValue.builder().n("0").build());
					courseExpressionValues.put(":inc", AttributeValue.builder().n("0").build());
					// dropped means seat was occupied , will still count as seat sold.
				}else {
					conditionExpression = "#maxSeats > #seats";
					courseExpressionNames.put("#maxSeats", CourseTableConstants.MAX_SEATS);
					courseExpressionNames.put("#seats", CourseTableConstants.SEATS_FILLED);
					courseExpressionValues.put(":inc", AttributeValue.builder().n("1").build());
					
				}
				
				// Build Update request for course seatsFilled
	            Update courseUpdate = Update.builder()
	                    .tableName(CourseTableConstants.TABLE_NAME)
	                    .key(Map.of(
	                            CourseTableConstants.COURSE_ID,
	                            AttributeValue.builder().s(enrollment.getCourseId()).build()
	                    ))
	                    .updateExpression("ADD #seats :inc")
	                    .conditionExpression(conditionExpression) 
	                    .expressionAttributeValues(courseExpressionValues)
	                    .expressionAttributeNames(courseExpressionNames)
	                    .build();
	            
	            TransactWriteItemsRequest writeItemsRequest = TransactWriteItemsRequest.builder()
	            		.transactItems(
	            				TransactWriteItem.builder().update(enrollmentUpdate).build(),
	            				TransactWriteItem.builder().update(courseUpdate).build()
	            				)
	            		.build();
	            
	            client.transactWriteItems(writeItemsRequest);
	            		
			}
	    	
	    	
	    	else {
				 UpdateItemRequest request = UpdateItemRequest.builder()
				    		.tableName(EnrollmentTableConstants.TABLE_NAME)
				    		.key(key)
				    		.updateExpression(updateExpression.toString())
				    		.expressionAttributeValues(expressionValues)
				    		.expressionAttributeNames(expressionNames)
				    		.build();
				 client.updateItem(request);
			}
	        return true;
	    }catch (TransactionCanceledException e) {
			return false;
		}
	    
	    catch (Exception e) {
	        throw new DatabaseUnknownException("Unknown Error Try Later");
	    }
	}


	@Override
	public int getEnrollmentCountByStudentIdAndStatus(String studentId, EnrollmentStatus status) {
		QueryRequest request = QueryRequest.builder()
				.tableName(EnrollmentTableConstants.TABLE_NAME)
				.keyConditionExpression("#studentId = :studentId")
				.filterExpression("#status = :status")
				.expressionAttributeNames(Map.of(
						"#studentId",EnrollmentTableConstants.STUDENT_ID,
						"#status", EnrollmentTableConstants.STATUS
						))
				.expressionAttributeValues(Map.of(
						":studentId",AttributeValue.builder().s(studentId).build(),
						":status", AttributeValue.builder().s(status.toString()).build()
						))
				.select(Select.COUNT)
				.build();
		try {
			QueryResponse response = client.query(request);
			return response.count();
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error Try Later");
		}
	}



	
	private Enrollment itemToEnrollment(Map<String, AttributeValue> item) {
	    Enrollment enrollment = new Enrollment();

	   // always present
	    enrollment.setStudentId(item.get(EnrollmentTableConstants.STUDENT_ID).s());
	    enrollment.setCourseId(item.get(EnrollmentTableConstants.COURSE_ID).s());
	    enrollment.setStatus(item.get(EnrollmentTableConstants.STATUS).s());

	    // Optional Fields
	    AttributeValue positionAttr = item.get(EnrollmentTableConstants.POSITION_WAITLIST);
	    if (positionAttr != null && positionAttr.n() != null) {
	        enrollment.setPositionInWaitlist(Integer.parseInt(positionAttr.n()));
	    }

	    AttributeValue waitlistedAtAttr = item.get(EnrollmentTableConstants.WAITLISTED_AT);
	    if (waitlistedAtAttr != null && waitlistedAtAttr.s() != null) {
	        enrollment.setWaitlistedAt(waitlistedAtAttr.s());
	    }

	    AttributeValue enrolledAtAttr = item.get(EnrollmentTableConstants.ENROLLED_AT);
	    if (enrolledAtAttr != null && enrolledAtAttr.s() != null) {
	        enrollment.setEnrolledAt(enrolledAtAttr.s());
	    }

	    AttributeValue droppedAtAttr = item.get(EnrollmentTableConstants.DROPPED_AT);
	    if (droppedAtAttr != null && droppedAtAttr.s() != null) {
	        enrollment.setDroppedAt(droppedAtAttr.s());
	    }

	    AttributeValue optedOutAtAttr = item.get(EnrollmentTableConstants.OPTED_OUT_AT);
	    if (optedOutAtAttr != null && optedOutAtAttr.s() != null) {
	        enrollment.setOptedOutAt(optedOutAtAttr.s());
	    }

	    return enrollment;
	}

	private void appendUpdateField(StringBuilder expr, Map<String, AttributeValue> values, Map<String, String> names,
			String attrName, AttributeValue attrValue) {
		if (attrValue == null) return;
		
		if (expr.length() > 4) { // "SET " is length 4
			expr.append(", ");
		}
		expr.append("#").append(attrName).append(" = :").append(attrName);
		names.put("#"+attrName, attrName);
		values.put(":" + attrName, attrValue);
	}


	


}
