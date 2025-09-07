package enrollment.courseenrollment.repository.dynamodb;

import java.util.Map;
import enrollment.courseenrollment.exceptions.DatabaseUnknownException;
import enrollment.courseenrollment.exceptions.StudentIDAlreadyExistsException;
import enrollment.courseenrollment.model.Student;
import enrollment.courseenrollment.repository.StudentRepository;
import enrollment.courseenrollment.repository.dynamodb.constants.StudentTableConstants;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class StudentRepoDynamoDb implements StudentRepository {
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	@Override
	public boolean createStudent(Student student) {
		try {
			PutItemRequest request = PutItemRequest.builder()
					.tableName(StudentTableConstants.TABLE_NAME)
					.item(Map.of(
							StudentTableConstants.STUDENT_ID, AttributeValue.builder().s(student.getStudentId()).build(),
							StudentTableConstants.NAME, AttributeValue.builder().s(student.getName()).build(),
							StudentTableConstants.EMAIL, AttributeValue.builder().s(student.getEmail()).build(),
							StudentTableConstants.PASSWORD_HASH, AttributeValue.builder().s(student.getPasswordHash()).build()
							))
					.conditionExpression("attribute_not_exists(#pk)")
					.expressionAttributeNames(Map.of(
							"#pk", StudentTableConstants.STUDENT_ID))
					.build();
			client.putItem(request);
			return true;
		}
		catch (ConditionalCheckFailedException e) {
			throw new StudentIDAlreadyExistsException("StudentId Exists, generate new one");
		}
		catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error, Try again");
		}
		
	}

	@Override
	public Student getStudentById(String studentId) {
		GetItemRequest request = GetItemRequest.builder()
				.tableName(StudentTableConstants.TABLE_NAME)
				.key(Map.of(StudentTableConstants.STUDENT_ID, AttributeValue.builder().s(studentId).build()))
				.build();
		try {
			GetItemResponse response = client.getItem(request);
			if (!response.hasItem())
				return null;
			Map<String, AttributeValue> item = response.item();
			Student  student = new Student();
			student.setStudentId(item.get(StudentTableConstants.STUDENT_ID).s());
			student.setEmail(item.get(StudentTableConstants.EMAIL).s());
			student.setName(item.get(StudentTableConstants.NAME).s());
			student.setPasswordHash(item.get(StudentTableConstants.PASSWORD_HASH).s());
			return student;
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error , Try again");
		}
	}

	@Override
	public Student getStudentByEmail(String email) {
		QueryRequest request = QueryRequest.builder()
				.tableName(StudentTableConstants.TABLE_NAME)
				.indexName(StudentTableConstants.EMAIL_INDEX)
				.keyConditionExpression("email = :email")
				.expressionAttributeValues(Map.of(":email" , AttributeValue.fromS(email)))
				.build();
		try {
			
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
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error , Try later");
		}
	}

	@Override
	public Student updateStudent(Student student) {
		UpdateItemRequest request = UpdateItemRequest.builder()
				.tableName(StudentTableConstants.TABLE_NAME)
				.key(Map.of(StudentTableConstants.STUDENT_ID,
						AttributeValue.builder().s(student.getStudentId()).build()))
				.updateExpression("Set #n = :name ,"+StudentTableConstants.EMAIL+" = :email")
				.expressionAttributeNames(Map.of("#n", StudentTableConstants.NAME))
				.expressionAttributeValues(Map.of(
						":name", AttributeValue.builder().s(student.getName()).build(),
						":email" , AttributeValue.builder().s(student.getEmail()).build()
						))
				.returnValues(ReturnValue.ALL_NEW)
				.build();
		try {
			UpdateItemResponse response = client.updateItem(request);
			Map<String, AttributeValue> updatedItem = response.attributes();
			student.setEmail(updatedItem.get(StudentTableConstants.EMAIL).s());
			student.setName(updatedItem.get(StudentTableConstants.NAME).s());
			return student;
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error , Try again");
		}
	}

	@Override
	public boolean updateStudentPassword(String studentId, String passwordHash) {
		UpdateItemRequest request = UpdateItemRequest.builder()
				.tableName(StudentTableConstants.TABLE_NAME)
				.key(Map.of(StudentTableConstants.STUDENT_ID,AttributeValue.builder().s(studentId).build()))
				.updateExpression("Set "+StudentTableConstants.PASSWORD_HASH+" = :passwordHash")
				.expressionAttributeValues(Map.of(":passwordHash", AttributeValue.builder().s(passwordHash).build()))
				.build();
		try {
			client.updateItem(request);
			return true;
		} catch (Exception e) {
			throw new DatabaseUnknownException("Unknown Error , Try Again");
		}
	}

}
