# Built from the repo root (one Gradle build for app + backend):
#   docker build -f backend/Dockerfile .
# Stage 1: build the jar with Gradle (Temurin 17 is the daemon JVM in gradle/gradle-daemon-jvm.properties)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
COPY backend/build.gradle.kts backend/
RUN chmod +x gradlew && ./gradlew --no-daemon :backend:dependencies > /dev/null
COPY backend/src backend/src
RUN ./gradlew --no-daemon :backend:bootJar && cp backend/build/libs/backend-*-SNAPSHOT.jar app.jar

# Stage 2: run only the jar on a JRE; no Gradle, no source
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home spring
COPY --from=build /workspace/app.jar app.jar
USER spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fs http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
