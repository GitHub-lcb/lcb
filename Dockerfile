FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY backend/lcb-admin/target/lcb-admin.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
