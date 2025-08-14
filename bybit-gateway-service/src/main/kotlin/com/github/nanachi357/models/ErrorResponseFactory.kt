package com.github.nanachi357.models

/**
 * Factory for creating standardized error responses.
 * 
 * Provides consistent error response creation with proper context
 * and debugging information for different error scenarios.
 */
object ErrorResponseFactory {
    
    /**
     * Creates a validation error response.
     * 
     * @param field The field that failed validation
     * @param message The validation error message
     * @param path The request path (optional)
     * @return ApiResponse.Error with validation error details
     */
    fun validationError(
        field: String, 
        message: String, 
        path: String? = null
    ): ApiResponse.Error {
        return ApiResponse.Error(
            message = "Invalid $field: $message",
            code = "VALIDATION_ERROR",
            path = path,
            details = mapOf("field" to field)
        )
    }
    
    /**
     * Creates a not found error response.
     * 
     * @param resource The resource type that was not found
     * @param identifier The identifier that was searched for
     * @param path The request path (optional)
     * @return ApiResponse.Error with not found details
     */
    fun notFound(
        resource: String, 
        identifier: String, 
        path: String? = null
    ): ApiResponse.Error {
        return ApiResponse.Error(
            message = "$resource not found: $identifier",
            code = "NOT_FOUND",
            path = path,
            details = mapOf(
                "resource" to resource,
                "identifier" to identifier
            )
        )
    }
    
    /**
     * Creates an external API error response.
     * 
     * @param service The external service name
     * @param message The error message from the external service
     * @param path The request path (optional)
     * @return ApiResponse.Error with external API error details
     */
    fun externalApiError(
        service: String, 
        message: String,
        path: String? = null
    ): ApiResponse.Error {
        return ApiResponse.Error(
            message = "External service error ($service): $message",
            code = "EXTERNAL_API_ERROR",
            path = path,
            details = mapOf("service" to service)
        )
    }
    
    /**
     * Creates a market data error response.
     * 
     * @param symbol The symbol that caused the error
     * @param message The error message
     * @param path The request path (optional)
     * @return ApiResponse.Error with market data error details
     */
    fun marketDataError(
        symbol: String, 
        message: String,
        path: String? = null
    ): ApiResponse.Error {
        return ApiResponse.Error(
            message = "Market data error for $symbol: $message",
            code = "MARKET_DATA_ERROR",
            path = path,
            details = mapOf("symbol" to symbol)
        )
    }
    
    /**
     * Creates a server time error response.
     * 
     * @param message The error message
     * @param path The request path (optional)
     * @return ApiResponse.Error with server time error details
     */
    fun serverTimeError(
        message: String,
        path: String? = null
    ): ApiResponse.Error {
        return ApiResponse.Error(
            message = "Server time error: $message",
            code = "SERVER_TIME_ERROR",
            path = path
        )
    }
    
    /**
     * Creates a generic error response.
     * 
     * @param message The error message
     * @param code The error code
     * @param path The request path (optional)
     * @param details Additional error details (optional)
     * @return ApiResponse.Error with generic error details
     */
    
}
