# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package \
    && cp target/santos-chamados-*.jar /workspace/app.jar

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app --shell /usr/sbin/nologin app \
    && mkdir -p /data/anexos /app/logs \
    && chown -R app:app /app /data

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m" \
    SERVER_PORT=8080 \
    TZ=America/Sao_Paulo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT:-8080} && printf 'GET /actuator/health HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n' >&3 && cat <&3 | grep -q 'UP'"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
