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

# Install curl, netcat and cloud-sql-proxy
RUN apk add --no-cache curl wget netcat-openbsd && \
    wget -q https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O /cloud_sql_proxy && \
    chmod +x /cloud_sql_proxy

# Copy JAR from builder
COPY --from=builder /app/target/user-management-api-*.jar app.jar

# Create a non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

# Copy the startup script
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

USER appuser

# Expose port 8080 (Cloud Run default)
EXPOSE 8080

# Set environment variables for Cloud Run
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=mysql \
    LOG_LEVEL=INFO

# Use the startup script as entrypoint
ENTRYPOINT ["/app/start.sh"]
