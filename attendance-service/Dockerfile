FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/attendance-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
