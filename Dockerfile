FROM maven:3.6.3-jdk-11-slim as builder

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN ["mvn", "package"]

FROM openjdk:11-jdk-slim
COPY --from=builder /code/target/moneytransfer-jar-with-dependencies.jar app.jar

EXPOSE 4567
CMD ["/usr/local/openjdk-11/bin/java", "-jar", "app.jar"]