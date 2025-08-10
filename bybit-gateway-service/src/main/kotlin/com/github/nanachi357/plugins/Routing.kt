package com.github.nanachi357.plugins

import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.ServerStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Application.configureRouting() {
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
    }
}