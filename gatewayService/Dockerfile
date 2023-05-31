FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/*.jar .

EXPOSE 8080

CMD ["java","-jar","/app/gatewayService-1.0-SNAPSHOT.jar"]
