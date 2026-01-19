# Vulnerable Log4Shell Application
# Uses Java 8u181 which doesn't have the JNDI restrictions added in later versions
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

# Build the vulnerable app
RUN mvn clean package -DskipTests

# Runtime image - using old Java 8 without security restrictions
FROM openjdk:8u181-jre-alpine

WORKDIR /app

# Copy built jar
COPY --from=builder /build/target/log4shell-vulnerable-app-1.0.0.jar app.jar

EXPOSE 8080

# Critical: These flags allow JNDI to load remote code
# In newer Java versions, this is blocked by default
ENV JAVA_OPTS="-Dcom.sun.jndi.ldap.object.trustURLCodebase=true \
               -Dcom.sun.jndi.rmi.object.trustURLCodebase=true"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
