FROM amazoncorretto:17-alpine-jdk
WORKDIR /opt/app
ARG JAR_FILE=target/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]