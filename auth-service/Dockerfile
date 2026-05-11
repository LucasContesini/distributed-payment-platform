FROM gradle:8.11.1-jdk21-alpine AS build
WORKDIR /workspace
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY shared-libs ./shared-libs
COPY auth-service ./auth-service
RUN gradle :auth-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
