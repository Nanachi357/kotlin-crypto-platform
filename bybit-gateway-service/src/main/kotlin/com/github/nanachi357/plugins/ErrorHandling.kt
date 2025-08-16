package com.github.nanachi357.plugins

import com.github.nanachi357.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import java.util.concurrent.TimeoutException

private val logger = KotlinLogging.logger {}

/**
 * Custom exception for not found errors
 */
class NotFoundException(message: String) : Exception(message)

/**
 * Custom exception for parameter conversion errors
 */
class ParameterConversionException(message: String) : Exception(message)

/**
 * Ktor plugin for centralized exception handling.
 * 
 * Provides consistent error responses with proper HTTP status codes
 * for different types of exceptions that may occur in the application.
 */
fun Application.configureErrorHandling() {
    install(StatusPages) {
        
        // Handle validation errors (400 Bad Request)
        exception<IllegalArgumentException> { call, exception ->
            val path = call.request.local.uri
            val error = ApiResponse.Error(
                message = exception.message ?: "Invalid input provided",
                code = "VALIDATION_ERROR",
                path = path
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        
        // Handle serialization errors (400 Bad Request)
        exception<SerializationException> { call, exception ->
            val path = call.request.local.uri
            val error = ApiResponse.Error(
                message = "Invalid request format: ${exception.message}",
                code = "SERIALIZATION_ERROR",
                path = path
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        
        // Handle missing parameters (400 Bad Request)
        exception<ParameterConversionException> { call, exception ->
            val path = call.request.local.uri
            val error = ApiResponse.Error(
                message = "Missing or invalid parameter: ${exception.message}",
                code = "PARAMETER_ERROR",
                path = path
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        
        // Handle not found errors (404 Not Found)
        exception<NotFoundException> { call, exception ->
            val path = call.request.local.uri
            val error = ApiResponse.Error(
                message = exception.message ?: "Resource not found",
                code = "NOT_FOUND",
                path = path
            )
            call.respond(HttpStatusCode.NotFound, error)
        }
        
        // Handle timeout errors (504 Gateway Timeout)
        exception<TimeoutException> { call, exception ->
            val path = call.request.local.uri
            val error = ApiResponse.Error(
                message = "Request timeout: ${exception.message}",
                code = "TIMEOUT_ERROR",
                path = path
            )
            call.respond(HttpStatusCode.GatewayTimeout, error)
        }
        
        // Handle all other exceptions (500 Internal Server Error)
        exception<Exception> { call, exception ->
            val path = call.request.local.uri
            val userAgent = call.request.headers["User-Agent"] ?: "unknown"
            val remoteHost = call.request.local.remoteHost
            val requestId = call.request.headers["X-Request-ID"] ?: "no-id"
            
            logger.error(exception) { 
                "Unhandled exception: requestId=$requestId, path=$path, userAgent=$userAgent, remoteHost=$remoteHost" 
            }
            
            val error = ApiResponse.Error(
                message = "Internal server error: ${exception.message}",
                code = "INTERNAL_ERROR",
                path = path
            )
            call.respond(HttpStatusCode.InternalServerError, error)
        }
    }
}
