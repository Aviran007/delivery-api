# Delivery API

Spring Boot service for delivery scheduling with address resolution, timeslot management, and concurrency control.

## 🚀 Quick Start

```bash
make run       # Start the API
make test-api  # Test all endpoints
make stop      # Stop the API
```

The API will be available at `http://localhost:8080`

**Interactive API Documentation:** [Swagger UI](http://localhost:8080/swagger-ui.html)

## ✨ Features

- **Address Resolution**: Geoapify API integration with fallback parsing
- **Timeslot Management**: JSON-based configuration loaded at startup
- **Holiday Exclusion**: Integration with external holiday API
- **Capacity Management**: Configurable daily (10) and per-timeslot (2) limits
- **Concurrency Control**: Thread-safe booking with semaphore-based management

## 🔧 Prerequisites

- **Java 21** (JDK 21 or higher)
- **Gradle** (wrapper included)

### Installing Java 21

**macOS:**
```bash
brew install openjdk@21
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install openjdk-21-jdk
```

Verify: `java -version`

## ⚙️ Configuration

Configuration via environment variables or `application.properties`.

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | HTTP server port |
| `GEOAPIFY_API_KEY` | _(empty)_ | Address resolution API key (optional) |
| `HOLIDAY_API_KEY` | _(empty)_ | Holiday API key (optional) |
| `BUSINESS_DAILY_CAPACITY` | `10` | Maximum deliveries per day |
| `BUSINESS_TIMESLOT_CAPACITY` | `2` | Maximum deliveries per timeslot |

### API Keys Setup

**Geoapify API Key:**
- Used for production-grade address resolution
- Without key: falls back to naive string parsing
- Get your key at: https://www.geoapify.com/

**Holiday API Key:**
- Used for holiday validation and timeslot exclusion
- Without key: holiday filtering is disabled
- Get your key at: https://holidayapi.com/

**Full API documentation:** See [Swagger UI](http://localhost:8080/swagger-ui.html) for detailed request/response examples

### Custom Configuration Example

```bash
export BUSINESS_DAILY_CAPACITY=20
export BUSINESS_TIMESLOT_CAPACITY=5
export GEOAPIFY_API_KEY=your_geoapify_key
export HOLIDAY_API_KEY=your_holiday_key
make run
```

### Timeslot Configuration

Edit `src/main/resources/courier_timeslots.json` to configure available delivery timeslots. The file is loaded at application startup.

## 📡 API Endpoints

### Address
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/resolve-address` | Resolve free-text address to structured format |

### Timeslots
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/timeslots` | Get available timeslots for an address |

### Deliveries
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/deliveries` | Book a delivery in a timeslot |
| POST | `/deliveries/{id}/complete` | Mark delivery as completed |
| DELETE | `/deliveries/{id}` | Cancel a delivery |
| GET | `/deliveries/daily` | List today's deliveries |
| GET | `/deliveries/weekly` | List current week's deliveries |

## 🧪 Testing

```bash
make test      # Run all tests
make test-api  # Test API endpoints
```

## 🐳 Docker

```bash
make docker-build  # Build Docker image
make docker-run    # Run in Docker container
```

## 🏗️ Architecture

**Package Structure:**
- `api/` - REST controllers and DTOs
- `application/` - Business logic services
- `domain/` - Domain models and repositories
- `infrastructure/` - External clients, configuration, and exceptions

**Key Patterns:**
- Repository Pattern (in-memory ConcurrentHashMap)
- DTOs for API request/response separation
- Semaphore-based concurrency control
- Builder Pattern for domain models

## ❗ Troubleshooting

**Port 8080 already in use:**
```bash
export SERVER_PORT=8081
make run
```

**Address resolution not working:**
- Set `GEOAPIFY_API_KEY` or use fallback parsing (automatic)

**Holiday filtering not working:**
- Without `HOLIDAY_API_KEY`: Holiday filtering is disabled - all timeslots are shown
- Set `HOLIDAY_API_KEY` to enable holiday exclusion
- If API fails: System falls back to showing all timeslots (no errors)

**Java version issues:**
```bash
java -version  # Should show Java 21
# macOS: export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```
