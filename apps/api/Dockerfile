FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --create-home --uid 10001 guateque
COPY --from=build /workspace/target/*.jar /app/app.jar

RUN mkdir -p /app/uploads && chown -R guateque:guateque /app
USER guateque

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
