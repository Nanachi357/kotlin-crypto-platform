package com.github.nanachi357.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Standardized API response wrapper for consistent response structure.
 * 
 * @param T The type of data being returned in successful responses
 */
@Serializable
sealed class ApiResponse<out T> {
    
    /**
     * Successful response containing data.
     * 
     * @param data The actual response data
     */
    @Serializable
    @SerialName("success")
    data class Success<T>(val data: T) : ApiResponse<T>()
    
    /**
     * Error response containing error information.
     * 
     * @param message Human-readable error message
     * @param code Machine-readable error code for programmatic handling
     * @param timestamp When the error occurred (Unix timestamp in milliseconds)
     * @param path Request path for debugging context (optional)
     * @param details Additional context information (optional)
     */
    @Serializable
    @SerialName("error")
    data class Error(
        val message: String, 
        val code: String,
        val timestamp: Long = System.currentTimeMillis(),
        val path: String? = null,
        val details: Map<String, String>? = null
    ) : ApiResponse<Nothing>()
}

/**
 * Market API information response for base market endpoint.
 */
@Serializable
data class MarketApiInfo(
    val message: String,
    val endpoints: @Serializable Map<String, String>,
    val examples: @Serializable List<String>
)
