# Yoga Class Booking Portal – runtime image
# The WAR is built by Maven (locally or in Jenkins) and copied in; the Spring Boot
# executable WAR runs with `java -jar`.
FROM eclipse-temurin:21-jre

ARG APP_VERSION=dev
LABEL org.opencontainers.image.title="yoga-portal" \
      org.opencontainers.image.description="Yoga Class Booking Portal" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.authors="Gaurav Deore"

# Run as an unprivileged user
RUN groupadd --system yoga && useradd --system --gid yoga --home-dir /app --shell /usr/sbin/nologin yoga

WORKDIR /app
COPY --chown=yoga:yoga target/yoga-portal.war /app/app.war

ENV APP_ENVIRONMENT=docker \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
USER yoga

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
