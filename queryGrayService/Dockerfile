FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/queryGrayService-1.0-SNAPSHOT.jar .

EXPOSE 8088

CMD ["java","-jar","/app/queryGrayService-1.0-SNAPSHOT.jar"]
