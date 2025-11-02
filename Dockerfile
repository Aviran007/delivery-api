# Multi-stage build for optimized Docker image
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first (for better layer caching)
# This allows Docker to cache dependencies layer separately
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Copy source code (this layer will invalidate more often)
COPY src/ src/

# Build the application
# Using --no-daemon for Docker builds
# Note: No need for 'clean' - each build starts fresh
RUN chmod +x gradlew && \
    ./gradlew bootJar --no-daemon

# Runtime stage - use JRE only (smaller image ~50% reduction)
FROM eclipse-temurin:21-jre-alpine

# Metadata labels
LABEL org.opencontainers.image.description="Delivery Scheduling API"
LABEL org.opencontainers.image.version="1.0.0"

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/delivery-api-*.jar app.jar

# Change ownership of app directory and JAR to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user (security best practice)
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check - checks if API is responding
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/deliveries/daily || exit 1

# Run the application with JVM optimizations for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
