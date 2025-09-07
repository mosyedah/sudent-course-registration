package enrollment.courseenrollment.repository.dynamodb;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.repository.LogRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.StudentLogTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class LogRepoDynamoDb implements LogRepository {
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	@Override
	public void createLog(StudentLog log) {
	    Map<String, AttributeValue> item = new HashMap<>();

	    
	    item.put(StudentLogTableConstants.LOG_ID, 
	             AttributeValue.builder().s(UUID.randomUUID().toString()).build());
	    
	    item.put(StudentLogTableConstants.TIMESTAMP, 
	    		AttributeValue.builder().s(LocalDateTime.now().toString()).build());
	    
	    putIfNotNull(item, StudentLogTableConstants.STUDENT_ID, log.getStudentId());
	    putIfNotNull(item, StudentLogTableConstants.COURSE_ID, log.getCourseId());
	    putIfNotNull(item, StudentLogTableConstants.ACTION, log.getAction().toString());
	    putIfNotNull(item, StudentLogTableConstants.DESCRIPTION, log.getDescription());

	    PutItemRequest request = PutItemRequest.builder()
	            .tableName(StudentLogTableConstants.TABLE_NAME)
	            .item(item)
	            .build();

	    try {
	        client.putItem(request);
	    } catch (Exception e) {
	        // its okay if log is kipped fo rnow
	    }
	}

	// Helper method to add attribute only if it is not null
	private void putIfNotNull(Map<String, AttributeValue> map, String key, String value) {
	    if (value != null) {
	        map.put(key, AttributeValue.builder().s(value).build());
	    }
	}


}
