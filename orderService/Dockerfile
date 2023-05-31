FROM openjdk:8-jre

MAINTAINER Vzerx

WORKDIR /app

ADD target/orderService-1.0-SNAPSHOT.jar .

EXPOSE 8380

CMD ["java","-jar","/app/orderService-1.0-SNAPSHOT.jar"]
