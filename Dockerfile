# -------- Build stage --------
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package \
 && echo "Built artifacts:" && ls -lh target \
 && JAR="$(ls -1 target/*-shaded.jar 2>/dev/null || true)"; \
    if [ -z "$JAR" ]; then \
      # if shade replaces the main artifact, pick non-original jar
      JAR="$(ls -1 target/*.jar | grep -v 'original-' | head -n1)"; \
    fi; \
    cp "$JAR" app.jar \
 && (jar tf app.jar | grep -q '^org/h2/Driver.class$' || (echo 'ERROR: H2 driver not found in shaded JAR' && exit 1)) \
 && (jar xf app.jar META-INF/MANIFEST.MF && echo 'Manifest:' && cat META-INF/MANIFEST.MF | grep -i Main-Class || true)

# -------- Run stage --------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar

ENV PORT=8080 \
    ADMIN_USER=admin \
    ADMIN_PASS=admin \
    JAVA_OPTS=""

EXPOSE 8080
# Use -jar so the manifest Main-Class is honored
CMD ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]
