# Build the Spring Boot executable WAR.
FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Run the application on Render's assigned port (default 10000).
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.war /app/app.war
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+UseSerialGC"
EXPOSE 10000
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT:-10000} -jar /app/app.war"]
