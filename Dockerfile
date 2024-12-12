# Use a minimal OpenJDK runtime image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Uberjar into the image
COPY target/app.jar .
COPY resources ./resources

# Expose the application's port
EXPOSE 8890

# Run the Uberjar
CMD ["java", "-jar", "app.jar"]


