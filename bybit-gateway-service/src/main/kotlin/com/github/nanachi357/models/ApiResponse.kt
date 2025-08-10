package com.github.nanachi357.models

import kotlinx.serialization.Serializable

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
    data class Success<T>(val data: T) : ApiResponse<T>()
    
    /**
     * Error response containing error information.
     * 
     * @param message Human-readable error message
     * @param code Machine-readable error code for programmatic handling
     */
    @Serializable
    data class Error(val message: String, val code: String) : ApiResponse<Nothing>()
}
