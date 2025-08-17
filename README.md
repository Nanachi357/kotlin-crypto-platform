# Kotlin Crypto Platform

**Cryptocurrency trading platform built with Kotlin and Ktor**

## Project Status

**Current Phase:** Phase 1 - Core API Integration  
**Implementation:** Bybit Gateway Service  
**Status:** ✅ Completed - Production Ready

## Architecture

```
kotlin-crypto-platform/
├── bybit-gateway-service/          # Primary gateway service
│   ├── src/main/kotlin/com/github/nanachi357/  # Application source
│   │   ├── Application.kt          # Main application entry point
│   │   ├── clients/                # External API clients
│   │   ├── models/                 # Data models and responses
│   │   ├── plugins/                # Ktor plugins (routing, error handling)
│   │   ├── services/               # Business logic services
│   │   ├── utils/                  # Utility classes
│   │   └── validation/             # Input validation
│   ├── src/main/resources/         # Configuration files
│   ├── testing/                    # Comprehensive test suite
│   │   ├── comprehensive-postman-collection.json # 25+ automated tests
│   │   └── README.md               # Testing documentation
│   ├── build.gradle.kts            # Build configuration
│   └── settings.gradle.kts         # Project settings
├── gradle.properties               # Global properties
├── .gitignore                      # Git exclusions
└── LICENSE                         # MIT License
```

## Technology Stack

| Component | Technology | Version | Status |
|-----------|------------|---------|--------|
| **Language** | Kotlin | 1.9.22 | ✅ Configured |
| **Framework** | Ktor Server/Client | 2.3.7 | ✅ Implemented |
| **Build** | Gradle | 8.5 | ✅ Configured |
| **Serialization** | kotlinx.serialization | Latest | ✅ Implemented |
| **HTTP Client** | OkHttp Engine | Latest | ✅ Implemented |
| **Logging** | Logback Classic | 1.4.14 | ✅ Configured |
| **Target Exchange** | Bybit V5 API | Latest | ✅ Integrated |

## Current Implementation

### ✅ Completed (Production Ready)
- **Core Infrastructure**
  - Ktor server setup with basic endpoints (`/health`, `/`)
  - JSON configuration and serialization setup
  - HTTP client foundation for external API calls
  - Project structure and build configuration

- **Market Data Integration**
  - Bybit V5 API client with HTTP client
  - Market data models and response structures
  - Single symbol price endpoints (`/api/market/{symbol}`)
  - Multiple symbols support (`/api/market/tickers`)
  - Server time synchronization (`/bybit-time`)

- **Batch API with Domain Categories**
  - Adaptive processing strategies (single/parallel/batch)
  - Domain categories support (SPOT, LINEAR, INVERSE)
  - Structured API responses with metadata
  - Performance optimization for different symbol counts

- **Error Handling & Validation**
  - Centralized exception handling
  - Input validation with graceful degradation
  - Standardized error responses
  - Comprehensive error scenarios coverage

- **Logging & Monitoring**
  - Production-ready Logback configuration with file rotation
  - CallLogging for performance monitoring
  - Structured error logging with context
  - Performance timing and warnings
  - Request tracking with MDC

- **Testing Infrastructure**
  - 30+ comprehensive Postman tests
  - Performance testing with timing validation
  - Edge cases and stress testing
  - Logging verification tests
  - Automated validation with JavaScript

### 🚧 In Development
- Authentication and security (HMAC-SHA256)
- Rate limiting implementation
- Advanced caching strategies

### 📋 Planned
- Database integration (MySQL + Ktorm)
- Caching layer (Redis)
- Real-time data streaming (WebSocket)
- Trading functionality
- Risk management features

## Development Setup

### Prerequisites
- JDK 11 or higher
- Gradle 8.0+ (wrapper included)

### Build and Run
```bash
cd kotlin-crypto-platform/bybit-gateway-service

# Build project
./gradlew build

# Run application
./gradlew run

# Run tests
./gradlew test
```

### Configuration
Application configuration in `src/main/resources/application.conf`:

```hocon
ktor {
    deployment {
        port = 8080
        environment = development
    }
}

bybit {
    api {
        baseUrl = "https://api.bybit.com"
        testnetUrl = "https://api-testnet.bybit.com"
        useTestnet = true
    }
}
```

## API Documentation

### Implemented Endpoints

#### Health & Basic
- `GET /health` - Server health check
- `GET /` - API information
- `GET /bybit-time` - Bybit server time

#### Market Data
- `GET /api/market/{symbol}` - Single symbol price data
- `GET /api/market/tickers?symbols={symbols}` - Multiple symbols data
- `GET /api/market/tickers` - All available symbols

#### Batch API
- `GET /api/market/batch?symbols={symbols}&category={category}` - Batch price data
  - **Adaptive Strategies**: Single (1), Parallel (2-3), Batch (4+)
  - **Domain Categories**: SPOT, LINEAR, INVERSE
  - **Performance**: < 2000ms single, < 4000ms batch
  - **Error Handling**: Graceful degradation with mixed valid/invalid symbols

#### Error Testing
- `GET /test/validation-error` - Validation error simulation
- `GET /test/not-found` - Not found error simulation
- `GET /test/timeout` - Timeout error simulation
- `GET /test/unhandled` - Unhandled exception simulation

### Response Examples

#### Successful Batch Response
```json
{
  "data": {
    "prices": [{"symbol": "BTCUSDT", "lastPrice": "43250.50"}],
    "metadata": {
      "strategy": "parallel",
      "category": "SPOT",
      "requestTimeMs": 245,
      "successRate": 1.0
    }
  }
}
```

#### Error Response
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid symbol format",
  "timestamp": 1704627890123,
  "path": "/api/market/invalid",
  "details": {"field": "symbol"}
}
```

## Testing

### Comprehensive Test Suite
- **30+ automated tests** covering all functionality
- **Performance testing** with timing validation
- **Edge cases** and stress testing
- **Error scenarios** and graceful degradation
- **Structural validation** of responses
- **Logging verification** and monitoring tests

### Quick Test Setup
1. Import `testing/comprehensive-postman-collection.json` into Postman
2. Set environment variable `baseUrl = http://localhost:8080`
3. Run entire collection

## Development Phases

### ✅ Phase 1: Basic API Integration (COMPLETED)
- [x] **Core Infrastructure** - Ktor server, configuration, HTTP client
- [x] **Market Data Integration** - Bybit API client, price endpoints, multiple symbols
- [x] **Batch API Implementation** - Adaptive strategies, domain categories, performance optimization
- [x] **Error Handling & Validation** - Centralized exceptions, input validation, graceful degradation
- [x] **Logging & Monitoring** - Production-ready logging, performance monitoring, structured error logging
- [x] **Testing Infrastructure** - Comprehensive Postman tests, performance validation, logging verification

### 🚧 Phase 2: Authentication & Private APIs
- [ ] HMAC-SHA256 authentication
- [ ] Private account endpoints
- [ ] Rate limiting implementation
- [ ] Advanced error handling

### 📋 Phase 3: Data Persistence
- [ ] Database schema design
- [ ] Ktorm ORM integration
- [ ] Data storage and retrieval
- [ ] Transaction management

### 📋 Phase 4: Performance & Caching
- [ ] Redis integration
- [ ] Response caching strategies
- [ ] Performance optimization
- [ ] Monitoring and metrics

### 📋 Phase 5: Real-time Features
- [ ] WebSocket implementation
- [ ] Live market data streams
- [ ] Event-driven architecture
- [ ] Message queue integration

### 📋 Phase 6: Trading Features
- [ ] Order placement and management
- [ ] Portfolio tracking
- [ ] Risk management
- [ ] Trading algorithms

## Architecture Principles

- **Modular Design**: Clean separation of concerns with layered architecture
- **Async-First**: Kotlin coroutines for concurrent operations
- **Configuration-Driven**: External configuration for all environments
- **Type Safety**: Leverage Kotlin's type system for reliability
- **Testing**: Comprehensive automated testing with Postman
- **Error Handling**: Centralized exception handling with graceful degradation
- **Performance**: Adaptive processing strategies based on input size

## Contributing

This project follows standard Kotlin and Ktor development practices:

- Kotlin coding conventions
- Ktor plugin architecture
- Gradle build system
- Comprehensive testing approach
- English documentation standards

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

**⚠️ IMPORTANT: This software is provided "as is" without warranty of any kind.**

- This software may contain bugs and vulnerabilities
- Use at your own risk
- Always test thoroughly before using with real funds
- Consider this experimental software
- The authors are not responsible for any losses, damages, or issues arising from the use of this software
- Users are responsible for their own trading decisions and risk management

## Terms of Use

By using this software, you agree that:
- You use this software at your own risk
- You are responsible for your own trading decisions
- You understand that cryptocurrency trading involves significant risks
- You will not hold the authors liable for any losses or damages
- You will comply with all applicable laws and regulations