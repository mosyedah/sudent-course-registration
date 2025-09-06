package enrollment.courseenrollment.repository.dynamodb;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;


public class DynamoDbConfig {
	private static DynamoDbClient client;
	private static final String URL = "http://localhost:8000";
	
	public static DynamoDbClient getClient() {
		if(client == null)
			client = DynamoDbClient.builder()
					.endpointOverride(URI.create(URL))
					.region(Region.AP_SOUTH_1)
					.credentialsProvider(StaticCredentialsProvider.create(
							AwsBasicCredentials.create("dummy", "dummy")))
					.build();	
		return client;
	}
}
