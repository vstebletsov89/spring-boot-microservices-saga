FROM openjdk:21-slim
WORKDIR /app
COPY target/auth-service.jar app.jar
COPY src/main/resources/compromised.bin /app/resources/compromised.bin
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
