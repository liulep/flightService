FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/queryService-1.0-SNAPSHOT.jar .

EXPOSE 8081

CMD ["java","-jar","/app/queryService-1.0-SNAPSHOT.jar"]
