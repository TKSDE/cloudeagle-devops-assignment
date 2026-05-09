# Use official Eclipse Temurin image for Java 17
FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app
COPY . .
# We use maven directly if mvnw is missing in this dummy repo
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/sync-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
