FROM maven:3-jdk-8-alpine as base

FROM base as dev
COPY infrastructure/docker/java-entrypoint.sh /entrypoint.sh
ENTRYPOINT [ "/entrypoint.sh" ]


## Build stage
#
FROM base as build

# Jenkins related permission handling
ARG JENKINS_GROUP_ID=1000
ARG JENKINS_USER_ID=1000
RUN addgroup -g $JENKINS_GROUP_ID jenkins && \
    adduser -D -u $JENKINS_USER_ID -G jenkins jenkins

WORKDIR /code
COPY ./pom.xml ./pom.xml

# fetch all dependencies
# RUN mvn dependency:go-offline -B

COPY common common/
COPY crawler-service crawler-service/
COPY data-service data-service/
COPY delivery-service delivery-service/
COPY infrastructure/docker/java-entrypoint.sh /entrypoint.sh
#RUN ./java-entrypoint.sh 
#RUN mvn -B install --projects common
COPY data-service/src/main/resources/application-deployment.properties data-service/src/main/resources/application.properties
RUN mvn -B package --projects data-service --also-make

## Testing stage on Jenkins
FROM build as test
ENTRYPOINT [ "/entrypoint.sh" ]


## Running stage
#
FROM openjdk:8-jre-alpine as buildapi

# Copy the built artifact from build image
COPY --from=build /code/data-service/target/dataservice.jar /app.jar

# OPTIONAL: copy dependencies so the thin jar won't need to re-download them
#COPY --from=build /code/.m2 /code/.m2

# set the startup command to run the API
CMD ["java","-jar","/app.jar"]
