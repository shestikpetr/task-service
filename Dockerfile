# syntax=docker/dockerfile:1

# Optional Gradle init scripts, empty by default (CI uses Maven Central directly).
# Locally it can be replaced with your own scripts, e.g. a Maven Central mirror:
#   docker build --build-context gradle-init=$HOME/.gradle/init.d .
FROM scratch AS gradle-init

# ---------- Stage 1: build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Build files first: this layer and the dependency download below
# are rebuilt only when the build configuration or dependencies change
COPY gradlew settings.gradle build.gradle gradle.lockfile ./
COPY gradle ./gradle
RUN --mount=type=bind,from=gradle-init,target=/root/.gradle/init.d \
    ./gradlew downloadDependencies --no-daemon --quiet

# Source code last: a code change invalidates only the layers from here on
COPY src ./src
RUN --mount=type=bind,from=gradle-init,target=/root/.gradle/init.d \
    ./gradlew bootJar --no-daemon -Pkotlin.compiler.execution.strategy=in-process

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Pull OS security patches that are released before the base image tag is rebuilt
RUN apk upgrade --no-cache \
    && addgroup -S app && adduser -S app -G app
USER app

# Only the executable jar; JDK, Gradle, sources and dev/test dependencies stay in the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
