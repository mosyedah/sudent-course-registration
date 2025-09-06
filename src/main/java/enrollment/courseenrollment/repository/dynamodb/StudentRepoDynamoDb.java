package enrollment.courseenrollment.repository.dynamodb;

import java.util.Map;

import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.repository.StudentRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.StudentTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class StudentRepoDynamoDb implements StudentRepository {
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	@Override
	public void createStudent(Student student) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Student getStudentById(String studentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Student getStudentByEmail(String email) {
		QueryRequest request = QueryRequest.builder()
				.tableName(StudentTableConstants.TABLE_NAME)
				.indexName(StudentTableConstants.EMAIL_INDEX)
				.keyConditionExpression("email = :email")
				.expressionAttributeValues(Map.of(":email" , AttributeValue.fromS(email)))
				.build();
		QueryResponse response = client.query(request);
		if(response.items().isEmpty())
			return null;
		Map<String, AttributeValue> item = response.items().get(0);
		Student student = new Student();
		student.setStudentId(item.get(StudentTableConstants.STUDENT_ID).s());
		student.setEmail(item.get(StudentTableConstants.EMAIL).s());
		student.setName(item.get(StudentTableConstants.NAME).s());
		student.setPasswordHash(item.get(StudentTableConstants.PASSWORD_HASH).s());
		return student;
	}

	@Override
	public Student updateStudent(Student student) {
		// TODO Auto-generated method stub
		return null;
	}

}
