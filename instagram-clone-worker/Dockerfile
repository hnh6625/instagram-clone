FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom trước để Docker có thể cache dependencies
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests


# Run time
FROM eclipse-temurin:21-jre

WORKDIR /app

# Chỉ lấy JAR từ build stage
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]