FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/userService-1.0-SNAPSHOT.jar .

EXPOSE 8082

CMD ["java","-jar","/app/userService-1.0-SNAPSHOT.jar"]
