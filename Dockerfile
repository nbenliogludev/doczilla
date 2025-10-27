# -------- Build stage --------
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Cache deps
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -DskipTests package \
 && echo "Built artifacts:" && ls -lh target \
 && JAR="$(ls -1 target/*-shaded.jar 2>/dev/null || true)"; \
    if [ -z "$JAR" ]; then \
      JAR="$(ls -1 target/*.jar | grep -v 'original-' | head -n1)"; \
    fi; \
    cp "$JAR" app.jar

# -------- Run stage --------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar

ENV PORT=8080 \
    ADMIN_USER=admin \
    ADMIN_PASS=admin \
    JAVA_OPTS=""

EXPOSE 8080
CMD ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]
