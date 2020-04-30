FROM maven:3.6.0-jdk-8-alpine AS build
RUN mkdir -p /opt/app
COPY ./ /opt/app
RUN mvn -f /opt/app/pom.xml clean package -Dmaven.test.skip=true -B

FROM openjdk:8u191-jdk-alpine3.9
RUN mkdir -p /opt/app
COPY --from=build /opt/app/target/routeplanner.jar /opt/app
ARG JDK8_SPECIFIC_CONFIG="-Djava.security.egd=file:/dev/./urandom -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions "
ARG DEFAULT_JAVA_OPTS="-Xms256m -Xmx1024m $JDK8_SPECIFIC_CONFIG"
ENV JAVA_OPTS=$DEFAULT_JAVA_OPTS

ENTRYPOINT java $JAVA_OPTS -jar /opt/app/routeplanner.jar
EXPOSE 8080

