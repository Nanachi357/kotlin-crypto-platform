package com.github.nanachi357.plugins

import com.github.nanachi357.services.PriceService
import com.github.nanachi357.services.BatchPriceService
import com.github.nanachi357.services.UniversalPriceService
import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.exchange.ExchangeApiInfo
import com.github.nanachi357.models.ServerStatus
import com.github.nanachi357.models.MarketApiInfo
import com.github.nanachi357.models.BatchApiResponse
import com.github.nanachi357.models.BatchMetadata
import com.github.nanachi357.models.PriceInfo
import com.github.nanachi357.models.toPriceInfo
import com.github.nanachi357.models.MarketCategory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.util.concurrent.TimeoutException

fun Application.configureRouting(priceService: PriceService, batchPriceService: BatchPriceService, universalPriceService: UniversalPriceService) {
    routing {
        // Health check endpoint with structured JSON response
        get("/health") {
            val status = ServerStatus(
                status = "OK",
                timestamp = Instant.now().toString(),
                version = "1.0.0",
                uptime = System.currentTimeMillis()
            )
            call.respond(ApiResponse.Success(status))
        }
        
        // Service information endpoint
        get("/") {
            call.respond(ApiResponse.Success("Bybit Gateway API - Phase 1"))
        }
        
        // Test error endpoint for error handling validation
        get("/test-error") {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ApiResponse.Error(
                    message = "Test error response for validation",
                    code = "TEST_ERROR"
                )
            )
        }
        
        // Bybit API endpoints - simplified with PriceService
        get("/bybit-time") {
            val response = priceService.getServerTime()
            call.respond(response)
        }
        
        // Base market endpoint - provide information about available endpoints
        get("/api/market") {
            val marketInfo = MarketApiInfo(
                message = "Bybit Market API",
                endpoints = mapOf(
                    "single_ticker" to "/api/market/{symbol}",
                    "multiple_tickers" to "/api/market/tickers?symbols=BTCUSDT,ETHUSDC"
                ),
                examples = listOf(
                    "/api/market/BTCUSDT",
                    "/api/market/tickers?symbols=BTCUSDT,ETHUSDC"
                )
            )
            call.respond(ApiResponse.Success(marketInfo))
        }
        
        // Market endpoint with trailing slash - redirect to base endpoint
        get("/api/market/") {
            val marketInfo = MarketApiInfo(
                message = "Bybit Market API",
                endpoints = mapOf(
                    "single_ticker" to "/api/market/{symbol}",
                    "multiple_tickers" to "/api/market/tickers?symbols=BTCUSDT,ETHUSDC"
                ),
                examples = listOf(
                    "/api/market/BTCUSDT",
                    "/api/market/tickers?symbols=BTCUSDT,ETHUSDC"
                )
            )
            call.respond(ApiResponse.Success(marketInfo))
        }
        
        get("/api/market/{symbol}") {
            val symbol = call.parameters["symbol"] ?: "BTCUSDT"
            val response = priceService.getMarketTicker(symbol)
            call.respond(response)
        }
        
        get("/api/market/tickers") {
            val symbols = call.request.queryParameters["symbols"]?.split(",") ?: emptyList()
            val response = priceService.getMarketTickers(symbols)
            call.respond(response)
        }
        
        // New batch processing endpoint with domain categories
        get("/api/market/batch") {
            val symbols = call.request.queryParameters["symbols"]?.split(",") ?: listOf("BTCUSDT", "ETHUSDT")
            val categoryParam = call.request.queryParameters["category"] ?: "SPOT"
            val category = MarketCategory.fromExchangeValue(categoryParam) ?: MarketCategory.SPOT
            
            val result = batchPriceService.getBatchPrices(symbols, category)
            
            val response = BatchApiResponse(
                prices = result.successful.map { it.toPriceInfo() },
                metadata = BatchMetadata(
                    strategy = result.strategy,
                    category = result.category.name,
                    requestTimeMs = result.requestTimeMs,
                    successCount = result.successful.size,
                    notFoundCount = result.notFound.size,
                    errorCount = result.errors.size,
                    successRate = result.successRate
                ),
                notFound = result.notFound,
                errors = result.errors
            )
            
            call.respond(ApiResponse.Success(response))
        }
                     
        // Test endpoints for error handling validation
        get("/test/validation-error") {
            throw IllegalArgumentException("Test validation error")
        }
        
        get("/test/not-found") {
            throw NotFoundException("Test not found error")
        }
        
        get("/test/timeout") {
            throw TimeoutException("Test timeout error")
        }
        
        get("/test/unhandled") {
            throw RuntimeException("Test unhandled exception")
        }
        
        // === UNIVERSAL EXCHANGE ABSTRACTION ENDPOINTS ===
        // New endpoints using universal response format
        
        // Universal market endpoint - new structure
        get("/api/v2/market/{symbol}") {
            val symbol = call.parameters["symbol"] ?: "BTCUSDT"
            val response = universalPriceService.getPrice(symbol)
            call.respond(response)
        }
        
        // Universal batch endpoint - new structure
        get("/api/v2/market/batch") {
            val symbols = call.request.queryParameters["symbols"]?.split(",") ?: listOf("BTCUSDT", "ETHUSDT")
            val response = universalPriceService.getPrices(symbols)
            call.respond(response)
        }
        
        // Universal market info endpoint
        get("/api/v2/market") {
            val marketInfo = ExchangeApiInfo(
                message = "Universal Exchange API - Phase 2",
                endpoints = mapOf(
                    "single_price" to "/api/v2/market/{symbol}",
                    "batch_prices" to "/api/v2/market/batch?symbols=BTCUSDT,ETHUSDT"
                ),
                examples = listOf(
                    "/api/v2/market/BTCUSDT",
                    "/api/v2/market/batch?symbols=BTCUSDT,ETHUSDT"
                ),
                format = "Universal ExchangeResponse format"
            )
            call.respond(marketInfo)
        }
    }
}