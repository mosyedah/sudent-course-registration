package enrollment.courseenrollment.repository.dynamodb;

import enrollment.courseenrollment.repository.dynamodb.constants.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class TableInitialiser {
	private static final DynamoDbClient client = DynamoDbConfig.getClient();
	
	public static void main(String[] args) {
//		createStudentTable();
//		createCourseTable();
//		createEnrollmentTable();
//		createStudentLogTable();
		
	}

	/*
	 * for faster lookup with email, i'll have GSI with email and have name, pass , studentId 
	 * as projections in GSI table.
	 */
	private static void createStudentTable() {
		try {
			CreateTableRequest request = CreateTableRequest.builder()
					.tableName(StudentTableConstants.TABLE_NAME)
					.keySchema(KeySchemaElement.builder()
							.attributeName(StudentTableConstants.STUDENT_ID)
							.keyType(KeyType.HASH)
							.build()
							)
					.attributeDefinitions(
							AttributeDefinition.builder()
							.attributeName(StudentTableConstants.STUDENT_ID)
							.attributeType(ScalarAttributeType.S)
							.build(),
							AttributeDefinition.builder()
							.attributeName(StudentTableConstants.EMAIL)
							.attributeType(ScalarAttributeType.S)
							.build()
							)
					.globalSecondaryIndexes(
							GlobalSecondaryIndex.builder()
							.indexName(StudentTableConstants.EMAIL_INDEX)
							.keySchema(KeySchemaElement.builder()
									.attributeName(StudentTableConstants.EMAIL)
									.keyType(KeyType.HASH)
									.build()
									)
							.projection(Projection.builder()
									.projectionType(ProjectionType.INCLUDE)
									.nonKeyAttributes(StudentTableConstants.STUDENT_ID, StudentTableConstants.NAME, StudentTableConstants.PASSWORD_HASH)
									.build()
									)
							.provisionedThroughput(ProvisionedThroughput.builder()
									.readCapacityUnits(5L)
									.writeCapacityUnits(5L)
									.build()
									)
							.build()		
							)
					.provisionedThroughput(ProvisionedThroughput.builder()
							.readCapacityUnits(5L)
							.writeCapacityUnits(5L)
							.build()
							)
					.build();
			
			client.createTable(request);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	/*
	 * nothig can be unique , so LogId is partition key ,
	 *  GSI on Student Id can be helpful for faster searches , projection as logId , StudentId
	 */
	private static void createStudentLogTable() {
		try {
			CreateTableRequest request = CreateTableRequest.builder()
					.tableName(StudentLogTableConstants.TABLE_NAME)
					.keySchema(KeySchemaElement.builder()
							.attributeName(StudentLogTableConstants.LOG_ID)
							.keyType(KeyType.HASH)
							.build()
							)
					.attributeDefinitions(AttributeDefinition.builder()
								.attributeName(StudentLogTableConstants.LOG_ID)
								.attributeType(ScalarAttributeType.S)
								.build(),
							AttributeDefinition.builder()
								.attributeName(StudentLogTableConstants.STUDENT_ID)
								.attributeType(ScalarAttributeType.S)
								.build()
							)
					.globalSecondaryIndexes(GlobalSecondaryIndex.builder()
							.indexName(StudentLogTableConstants.STUDENT_ID_INDEX)
							.keySchema(KeySchemaElement.builder()
									.attributeName(StudentLogTableConstants.STUDENT_ID)
									.keyType(KeyType.HASH)
									.build()
									)
							.projection(Projection.builder()
									.projectionType(ProjectionType.KEYS_ONLY)
									.build()
									)
							.provisionedThroughput(ProvisionedThroughput.builder()
									.readCapacityUnits(5L)
									.writeCapacityUnits(5L)
									.build()
									)
							.build()
							)
					.provisionedThroughput(ProvisionedThroughput.builder()
							.readCapacityUnits(5L)
							.writeCapacityUnits(5L)
							.build()
							)
					.build();
			client.createTable(request);
					
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	/*
	 * Im using StudentId + CourseId as primary key to prevent dup course enrollment
	 */
	private static void createEnrollmentTable() {
		
		try {
			CreateTableRequest request = CreateTableRequest.builder()
					.tableName(EnrollementTableConstants.TABLE_NAME)
					.keySchema(KeySchemaElement.builder()
							.attributeName(EnrollementTableConstants.STUDENT_ID)
							.keyType(KeyType.HASH)
							.build(),
							KeySchemaElement.builder()
							.attributeName(EnrollementTableConstants.COURSE_ID)
							.keyType(KeyType.RANGE)
							.build()
							)							
					.attributeDefinitions(AttributeDefinition.builder()
							.attributeName(EnrollementTableConstants.STUDENT_ID)
							.attributeType(ScalarAttributeType.S)
							.build(),
							AttributeDefinition.builder()
							.attributeName(EnrollementTableConstants.COURSE_ID)
							.attributeType(ScalarAttributeType.S)
							.build()
							)
					.provisionedThroughput(ProvisionedThroughput.builder()
							.readCapacityUnits(5L)
							.writeCapacityUnits(5L)
							.build()
							)
					.build();
			client.createTable(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void createCourseTable() {
		try {
			CreateTableRequest request = CreateTableRequest.builder()
					.tableName(CourseTableConstants.TABLE_NAME)
					.keySchema(KeySchemaElement.builder()
							.attributeName(CourseTableConstants.COURSE_ID)
							.keyType(KeyType.HASH)
							.build()
							)
					.attributeDefinitions(AttributeDefinition.builder()
							.attributeName(CourseTableConstants.COURSE_ID)
							.attributeType(ScalarAttributeType.S)
							.build()
							)
					.provisionedThroughput(ProvisionedThroughput.builder()
							.readCapacityUnits(5L)
							.writeCapacityUnits(5L)
							.build()
							)
					.build();
			client.createTable(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
