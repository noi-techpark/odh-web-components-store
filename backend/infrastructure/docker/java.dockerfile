FROM maven:3-jdk-8-alpine as base

## Local development stage
#
FROM base as dev
WORKDIR /code
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

COPY infrastructure/docker/java-entrypoint.sh /entrypoint.sh
RUN /entrypoint.sh true

WORKDIR /code
COPY ./pom.xml ./pom.xml

COPY common common/
COPY crawler-service crawler-service/
COPY data-service data-service/
COPY delivery-service delivery-service/
COPY data-service/src/main/resources/application-deployment.properties \
	 data-service/src/main/resources/application.properties
COPY delivery-service/src/main/resources/application-deployment.properties \
	 delivery-service/src/main/resources/application.properties	 
RUN chown -R ${JENKINS_USER_ID}:${JENKINS_GROUP_ID} /code

# fetch all dependencies (run the entrypoint.sh to force a .m2 location)
#RUN /entrypoint.sh true
# && mvn dependency:go-offline -B

USER ${JENKINS_USER_ID}:${JENKINS_GROUP_ID}
RUN mvn -B -Duser.home=/code install
RUN mvn -B -Duser.home=/code package --also-make
ENTRYPOINT [ "/entrypoint.sh" ]


## Running stage: API
#
FROM openjdk:8-jre-alpine as buildapi

# Copy the built artifact from build image
COPY --from=build /code/data-service/target/dataservice.jar /app.jar

# OPTIONAL: copy dependencies so the thin jar won't need to re-download them
#COPY --from=build /code/.m2 /code/.m2

# set the startup command to run the API
CMD ["java","-jar","/app.jar"]


## Running stage: CDN
#
FROM openjdk:8-jre-alpine as buildcdn

# Copy the built artifact from build image
COPY --from=build /code/delivery-service/target/deliveryservice.jar /app.jar

# OPTIONAL: copy dependencies so the thin jar won't need to re-download them
#COPY --from=build /code/.m2 /code/.m2

# set the startup command to run the API
CMD ["java","-jar","/app.jar"]