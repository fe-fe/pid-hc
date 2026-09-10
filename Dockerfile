FROM maven:3.9-eclipse-temurin-21 AS build
 
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM quay.io/wildfly/wildfly:39.0.0.Final-jdk21

COPY configure.cli /tmp/configure.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure.cli && \
    rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

COPY --from=build /app/target/*.war /opt/jboss/wildfly/standalone/deployments/
 
EXPOSE 8080
 