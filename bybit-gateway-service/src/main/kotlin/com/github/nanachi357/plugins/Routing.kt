package com.github.nanachi357.plugins

import com.github.nanachi357.services.PriceService
import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.ServerStatus
import com.github.nanachi357.models.MarketApiInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.util.concurrent.TimeoutException

fun Application.configureRouting(priceService: PriceService) {
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
    }
}