package enrollment.courseenrollment.repository.dynamodb;

import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import enrollment.courseenrollment.repository.dynamodb.constants.StudentTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class TablePopulator {

    private static final DynamoDbClient client = DynamoDbConfig.getClient();
    
    public static void main(String[] args) {
		populateAll();
	}

    public static void populateAll(){
    	ScanRequest request = ScanRequest.builder()
    			.tableName(StudentTableConstants.TABLE_NAME)
    			.limit(1)
    			.build();
    	try {
			ScanResponse response =  client.scan(request);
			if(response.count() == 0) 
			{
				populate("students.json", "Student");
				populate("enrollments.json", "Enrollment");
				populate("courses.json", "Course");				
			}else {
				System.out.println("Tables already has data");
			}
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    	finally {
//    		client.close();	will nto close else results in exception conncection pool close		
    		System.out.println("Operation complete");
		}
    }

    private static void populate(String jsonFile, String tableName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> items = mapper.readValue(
                Paths.get("src/main/resources/" + jsonFile).toFile(),
                List.class
        );

        for (Map<String, Object> item : items) {
            Map<String, AttributeValue> dynamoItem = new HashMap<>();

            for (Map.Entry<String, Object> entry : item.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    // Skip empty strings
                    if (value instanceof String && ((String) value).isBlank()) continue;

                    AttributeValue attr;
                    if (value instanceof Number) {
                        attr = AttributeValue.builder().n(String.valueOf(value)).build();
                    } else if (value instanceof Boolean) {
                        attr = AttributeValue.builder().bool((Boolean) value).build();
                    } else {
                        attr = AttributeValue.builder().s(value.toString()).build();
                    }

                    dynamoItem.put(entry.getKey(), attr);
                }
            }

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(dynamoItem)
                    .build();

            client.putItem(request);
            System.out.println("Inserted: " + item.get("studentId"));
        }
    }
}
