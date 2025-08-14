package com.github.nanachi357.plugins

import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.ErrorResponseFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import java.util.concurrent.TimeoutException

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
            val error = ErrorResponseFactory.validationError(
                field = "input",
                message = exception.message ?: "Invalid input provided",
                path = path
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        
        // Handle serialization errors (400 Bad Request)
        exception<SerializationException> { call, exception ->
            val path = call.request.local.uri
            val error = ErrorResponseFactory.validationError(
                field = "request",
                message = "Invalid request format: ${exception.message}",
                path = path
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        
        // Handle missing parameters (400 Bad Request)
        exception<ParameterConversionException> { call, exception ->
            val path = call.request.local.uri
            val error = ErrorResponseFactory.validationError(
                field = "parameter",
                message = "Missing or invalid parameter: ${exception.message}",
                path = path
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        
        // Handle not found errors (404 Not Found)
        exception<NotFoundException> { call, exception ->
            val path = call.request.local.uri
            val error = ErrorResponseFactory.notFound(
                resource = "endpoint",
                identifier = path,
                path = path
            )
            call.respond(HttpStatusCode.NotFound, error)
        }
        
        // Handle timeout errors (504 Gateway Timeout)
        exception<TimeoutException> { call, exception ->
            val path = call.request.local.uri
            val error = ErrorResponseFactory.externalApiError(
                service = "external",
                message = "Request timeout: ${exception.message}",
                path = path
            )
            call.respond(HttpStatusCode.GatewayTimeout, error)
        }
        
        // Handle all other exceptions (500 Internal Server Error)
        exception<Exception> { call, exception ->
            val path = call.request.local.uri
            println("Unhandled exception: ${exception.javaClass.simpleName} - ${exception.message}")
            exception.printStackTrace()
            
            val error = ApiResponse.Error(
                message = "Internal server error: ${exception.message}",
                code = "INTERNAL_ERROR",
                path = path
            )
            call.respond(HttpStatusCode.InternalServerError, error)
        }
    }
}
