# Kotlin Crypto Platform

**Cryptocurrency trading platform built with Kotlin and Ktor**

## Project Status

**Current Phase:** Initial Development  
**Implementation:** Bybit Gateway Service  
**Status:** Build infrastructure ready, core implementation in progress

## Architecture

```
kotlin-crypto-platform/
├── bybit-gateway-service/          # Primary gateway service
│   ├── src/main/kotlin/io/swapter/bybit/  # Application source
│   ├── src/test/kotlin/            # Test suite
│   ├── src/main/resources/         # Configuration files
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
| **Framework** | Ktor Server/Client | 2.3.7 | ✅ Configured |
| **Build** | Gradle | 8.5 | ✅ Configured |
| **Serialization** | kotlinx.serialization | Latest | ✅ Configured |
| **HTTP Client** | OkHttp | Latest | ✅ Configured |
| **Logging** | Logback Classic | 1.4.14 | ✅ Configured |
| **Target Exchange** | Bybit V5 API | Latest | 🚧 In Development |

## Current Implementation

### ✅ Completed
- Gradle build system with Kotlin DSL
- Ktor server and client dependencies
- JSON serialization configuration
- Project structure and packaging
- Development environment setup
- Application configuration framework

### 🚧 In Development
- Bybit API client implementation
- Market data endpoints
- Authentication and security
- Error handling and rate limiting

### 📋 Planned
- Database integration (MySQL + Ktorm)
- Caching layer (Redis)
- Real-time data streaming
- WebSocket implementation
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

# Run application (when implementation available)
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

## API Design

### Planned Endpoints

#### Market Data
- `GET /api/v1/market/ticker/{symbol}` - Current price data
- `GET /api/v1/market/orderbook/{symbol}` - Order book depth
- `GET /api/v1/market/klines/{symbol}` - Historical price data

#### Account (Authenticated)
- `GET /api/v1/account/balance` - Account balances
- `GET /api/v1/account/positions` - Current positions

## Development Phases

### Phase 1: Basic API Integration
- [x] Project setup and configuration
- [ ] Bybit API client implementation
- [ ] Public market data endpoints
- [ ] Basic error handling

### Phase 2: Authentication & Private APIs
- [ ] HMAC-SHA256 authentication
- [ ] Private account endpoints
- [ ] Rate limiting implementation
- [ ] Advanced error handling

### Phase 3: Data Persistence
- [ ] Database schema design
- [ ] Ktorm ORM integration
- [ ] Data storage and retrieval
- [ ] Transaction management

### Phase 4: Performance & Caching
- [ ] Redis integration
- [ ] Response caching strategies
- [ ] Performance optimization
- [ ] Monitoring and metrics

### Phase 5: Real-time Features
- [ ] WebSocket implementation
- [ ] Live market data streams
- [ ] Event-driven architecture
- [ ] Message queue integration

### Phase 6: Trading Features
- [ ] Order placement and management
- [ ] Portfolio tracking
- [ ] Risk management
- [ ] Trading algorithms

## Architecture Principles

- **Modular Design**: Clean separation of concerns
- **Async-First**: Kotlin coroutines for concurrent operations
- **Configuration-Driven**: External configuration for all environments
- **Type Safety**: Leverage Kotlin's type system for reliability
- **Testing**: Comprehensive unit and integration tests
- **Security**: Secure API key management and request signing

## Contributing

This project follows standard Kotlin and Ktor development practices:

- Kotlin coding conventions
- Ktor plugin architecture
- Gradle multi-project structure
- Comprehensive testing approach