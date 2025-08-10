package com.github.nanachi357.plugins

import com.github.nanachi357.clients.BybitApiClient
import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.ServerStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Application.configureRouting(bybitClient: BybitApiClient) {
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
        
        // Bybit API endpoints
        get("/bybit-time") {
            try {
                val serverTime = bybitClient.getServerTime()
                call.respond(ApiResponse.Success(serverTime))
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = ApiResponse.Error(
                        message = "Failed to get Bybit server time: ${e.message}",
                        code = "BYBIT_API_ERROR"
                    )
                )
            }
        }
        
        get("/api/market/{symbol}") {
            val symbol = call.parameters["symbol"] ?: "BTCUSDT"
            try {
                val marketData = bybitClient.getMarketTicker(symbol)
                call.respond(ApiResponse.Success(marketData))
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = ApiResponse.Error(
                        message = "Failed to fetch market data for $symbol: ${e.message}",
                        code = "MARKET_DATA_ERROR"
                    )
                )
            }
        }
        
        get("/api/market/tickers") {
            val symbols = call.request.queryParameters["symbols"]?.split(",") ?: emptyList()
            try {
                val marketData = bybitClient.getMarketTickers(symbols)
                call.respond(ApiResponse.Success(marketData))
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    message = ApiResponse.Error(
                        message = "Failed to fetch market data: ${e.message}",
                        code = "MARKET_DATA_ERROR"
                    )
                )
            }
        }
    }
}