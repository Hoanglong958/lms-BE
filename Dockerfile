FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Ensure gradlew is executable and run the build
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Find the generated jar that doesn't end with -plain.jar and rename to app.jar
RUN ls -l /app/build/libs/ && rm -f /app/build/libs/*-plain.jar && mv /app/build/libs/*.jar ./app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app.jar .
EXPOSE 3900
ENTRYPOINT ["java", "-jar", "app.jar"]
