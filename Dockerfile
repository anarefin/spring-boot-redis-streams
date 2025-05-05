# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file from the build context
# Assumes the jar is built in build/libs/
COPY build/libs/*.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Define environment variable
ENV JAVA_OPTS=""

# Run the jar file 
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ] 