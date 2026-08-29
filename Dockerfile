FROM maven:3.9-eclipse-temurin-11 AS build
 
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests
 
FROM quay.io/wildfly/wildfly:27.0.1.Final-jdk11

COPY --from=build /app/target/*.war /opt/jboss/wildfly/standalone/deployments/
 
EXPOSE 8080
 