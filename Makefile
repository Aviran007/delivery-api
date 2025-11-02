APP_NAME=delivery-api
DOCKER_IMAGE?=delivery-api:latest
GRADLEW=./gradlew
JAVA_HOME?=/usr/local/opt/openjdk@21

# Ensure Gradle wrapper exists
$(GRADLEW):
	@echo "Generating Gradle wrapper..."
	gradle wrapper --gradle-version 8.10.2

.PHONY: run stop test-api build test clean docker-build docker-run help

# Default target - show help
help:
	@echo "🚀 Delivery API - Quick Commands"
	@echo ""
	@echo "  make run          - Start the application (http://localhost:8080)"
	@echo "  make stop         - Stop the running application"
	@echo "  make test-api     - Test all API endpoints"
	@echo "  make test         - Run unit tests"
	@echo "  make build        - Build the project"
	@echo "  make clean        - Clean build artifacts"
	@echo ""
	@echo "📚 Docs: README.md | 🐳 Docker: make docker-build && make docker-run"

# Run the application with Java 21
run: $(GRADLEW)
	@echo "🚀 Starting Delivery API..."
	@export JAVA_HOME=$(JAVA_HOME) && export PATH="$(JAVA_HOME)/bin:$$PATH" && $(GRADLEW) bootRun

# Stop the running application
stop:
	@echo "🛑 Stopping Delivery API..."
	@if lsof -ti:8080 > /dev/null 2>&1; then \
		lsof -ti:8080 | xargs kill -9 2>/dev/null && echo "✅ Stopped process on port 8080" || true; \
	else \
		echo "ℹ️  No process found on port 8080"; \
	fi
	@pkill -f "gradle.*bootRun" 2>/dev/null && echo "✅ Stopped Gradle bootRun processes" || true
	@if lsof -ti:8080 > /dev/null 2>&1; then \
		echo "⚠️  Warning: Port 8080 may still be in use"; \
	else \
		echo "✅ Port 8080 is now free"; \
	fi

# Test the API endpoints
test-api:
	@if ! curl -s http://localhost:8080/deliveries/daily > /dev/null; then \
		echo "❌ API is not running. Start it with: make run"; \
		exit 1; \
	fi
	@echo "🧪 Testing API endpoints..."
	@chmod +x test-api.sh
	@./test-api.sh

# Build without tests (auto-generates wrapper if needed)
build: $(GRADLEW)
	$(GRADLEW) clean build -x test

# Run tests (auto-generates wrapper if needed)
test: $(GRADLEW)
	$(GRADLEW) test

# Clean build artifacts
clean:
	@if [ -f "$(GRADLEW)" ]; then \
		$(GRADLEW) clean; \
	else \
		echo "Gradle wrapper not found, nothing to clean"; \
	fi

# Build Docker image
docker-build:
	@echo "🐳 Building Docker image..."
	docker build -t $(DOCKER_IMAGE) .
	@echo "✅ Docker image built successfully: $(DOCKER_IMAGE)"

# Run Docker container (without API keys - uses fallback)
docker-run:
	@echo "🐳 Starting Docker container..."
	@echo "⚠️  Note: API keys not included - will use fallback mode"
	docker run -p 8080:8080 --rm $(DOCKER_IMAGE)

# Run Docker container with API keys from environment variables
docker-run-with-keys:
	@echo "🐳 Starting Docker container with API keys..."
	@if [ -z "$(GEOAPIFY_API_KEY)" ] && [ -z "$(HOLIDAY_API_KEY)" ]; then \
		echo "⚠️  Warning: No API keys found in environment variables"; \
		echo "   Set them with: export GEOAPIFY_API_KEY=... && export HOLIDAY_API_KEY=..."; \
	fi
	docker run -p 8080:8080 --rm \
		-e GEOAPIFY_API_KEY=$(GEOAPIFY_API_KEY) \
		-e HOLIDAY_API_KEY=$(HOLIDAY_API_KEY) \
		-e SPRING_PROFILES_ACTIVE=default \
		$(DOCKER_IMAGE)

# Stop Docker container
docker-stop:
	@echo "🛑 Stopping Docker containers..."
	@docker ps -q --filter ancestor=$(DOCKER_IMAGE) | xargs -r docker stop
	@echo "✅ Docker containers stopped"
