# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy the Maven wrapper and project files
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml .
COPY src src

# Make the wrapper executable (this is key for Render/Linux)
RUN chmod +x mvnw

# Build the project using the wrapper to ensure version consistency
RUN ./mvnw clean package -DskipTests

# Run stage - Use a smaller JRE for running the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
