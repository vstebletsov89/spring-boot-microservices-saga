FROM openjdk:21-slim
WORKDIR /app
COPY target/reservation-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
