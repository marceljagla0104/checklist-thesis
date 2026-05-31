# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk-alpine

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ADD /build/libs/boundary-1.0.jar checklist.jar

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/checklist.jar"]