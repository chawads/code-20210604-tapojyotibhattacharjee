FROM maven:3.5.2-jdk-8-alpine as builder
WORKDIR /app
COPY pom.xml /app/pom.xml
RUN mvn -B dependency:resolve dependency:resolve-plugins -T1.5C -Dmaven.repo.local=/tmp/m2
COPY src /app/src
RUN mvn test
RUN mvn -B -o -Dmaven.repo.local=/tmp/m2 -T1.5C package

FROM openjdk:8-jre-slim-buster
WORKDIR /app
COPY --from=builder /app/target/screening-bmi-report-0.0.1-SNAPSHOT.jar /app/screening-bmi-report.jar
EXPOSE 8082
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar screening-bmi-report.jar
