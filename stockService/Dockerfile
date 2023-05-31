FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/stockService-1.0-SNAPSHOT.jar .

EXPOSE 8480

CMD ["java","-jar","/app/stockService-1.0-SNAPSHOT.jar"]
