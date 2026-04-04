# Stage 1: Build the application JAR
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B -q

COPY src src
RUN ./mvnw package -DskipTests -B -q

# Stage 2: Extract Spring Boot layers for caching
FROM eclipse-temurin:25-jdk AS extract
WORKDIR /workspace
COPY --from=build /workspace/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract \
    --layers --destination extracted

# Stage 3: Minimal runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=extract /workspace/extracted/dependencies/ ./
COPY --from=extract /workspace/extracted/spring-boot-loader/ ./
COPY --from=extract /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extract /workspace/extracted/application/ ./

EXPOSE 9081
ENTRYPOINT ["java", "-jar", "app.jar"]
