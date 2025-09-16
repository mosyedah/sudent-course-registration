# Dockerfile - Spring Boot + DynamoDB Local
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install tools needed for DynamoDB Local
RUN apk add --no-cache wget unzip ca-certificates

# Download DynamoDB Local
RUN wget -q -O /tmp/dynamodb_local.tar.gz "https://s3.us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz" \
  && mkdir -p /dynamodb-local \
  && tar -xzf /tmp/dynamodb_local.tar.gz -C /dynamodb-local \
  && rm /tmp/dynamodb_local.tar.gz

# Copy your built JAR (Spring Boot app)
COPY target/*.jar /app/app.jar

# Expose ports
EXPOSE 8080 8000

# Start DynamoDB Local in background, then Spring Boot app
CMD java -Djava.library.path=/dynamodb-local/DynamoDBLocal_lib \
        -jar /dynamodb-local/DynamoDBLocal.jar -inMemory -port 8000 & \
    java -jar /app/app.jar
