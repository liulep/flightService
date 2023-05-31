FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/authService-1.0-SNAPSHOT.jar .

EXPOSE 8500

CMD ["java","-jar","/app/authService-1.0-SNAPSHOT.jar"]
