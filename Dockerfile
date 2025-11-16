# Use a Maven image with JDK 17 to build the application
# FROM maven:3.8.5-openjdk-17 AS builder
# WORKDIR /communication-service
# COPY pom.xml .
# COPY src ./src
# RUN mvn clean package -DskipTests

# Use a JDK runtime to run the application
FROM eclipse-temurin:17-jdk
WORKDIR /communication-service
COPY /target/*.jar communication-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "communication-service.jar"]


# FROM eclipse-temurin:17-jdk
# WORKDIR /communication-service
# COPY /target/*.jar communication-service.jar
# COPY entrypoint.sh /entrypoint.sh
# RUN chmod +x /entrypoint.sh
# EXPOSE 8080
# ENTRYPOINT ["/entrypoint.sh"]