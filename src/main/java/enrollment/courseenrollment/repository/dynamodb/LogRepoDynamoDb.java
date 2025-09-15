package enrollment.courseenrollment.repository.dynamodb;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import enrollment.courseenrollment.model.StudentLog;
import enrollment.courseenrollment.repository.LogRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.StudentLogTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
public class LogRepoDynamoDb implements LogRepository {
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	@Override
	public void createLog(StudentLog log) {
	    Map<String, AttributeValue> item = new HashMap<>();
	    //Always available
	    item.put(StudentLogTableConstants.LOG_ID, AttributeValue.builder().s(log.getLogId()).build());
	    item.put(StudentLogTableConstants.TIMESTAMP, AttributeValue.builder().s(log.getTimestamp().toString()).build());
	    
	    //Maybe null 
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
	        System.out.println("\n Exception From LogRepo Create Method" +e.getMessage());
	    }
	}

	// Helper method to add attribute only if it is not null
	private void putIfNotNull(Map<String, AttributeValue> map, String key, String value) {
	    if (value != null) {
	        map.put(key, AttributeValue.builder().s(value).build());
	    }
	}


}
