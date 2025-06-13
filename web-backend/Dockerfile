# Use the official OpenJDK image from the Docker Hub
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
# This assumes you've run 'mvn clean package' or 'gradle clean build'
# and that 'target/vms-0.1.1-SNAPSHOT.jar' is the correct path and filename.
COPY target/vms-0.1.1-SNAPSHOT.jar app.jar

# Expose the port the Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Define the command to run your application
# This will start your Spring Boot application inside the container.
# It automatically picks up 'application.yml' and any active profile files.
ENTRYPOINT ["java", "-jar", "app.jar"]