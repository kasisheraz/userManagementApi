# Multi-stage build for Cloud Run deployment
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install curl, netcat, and cloud-sql-proxy
RUN apk add --no-cache curl wget netcat-openbsd && \
    wget https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.13.0/cloud-sql-proxy.linux.amd64 -O /usr/local/bin/cloud-sql-proxy && \
    chmod +x /usr/local/bin/cloud-sql-proxy

# Copy JAR from builder
COPY --from=builder /app/target/user-management-api-*.jar app.jar

# Copy startup script
COPY start-with-proxy.sh /app/start-with-proxy.sh
RUN chmod +x /app/start-with-proxy.sh

# Create a non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expose port 8080 (Cloud Run default)
EXPOSE 8080

# Set environment variables for Cloud Run
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=mysql \
    LOG_LEVEL=INFO \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseStringDeduplication -Xss256k -XX:ReservedCodeCacheSize=64m"

# Use startup script with proxy
ENTRYPOINT ["/app/start-with-proxy.sh"]
