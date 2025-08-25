package com.github.nanachi357.utils

import kotlinx.coroutines.delay
import mu.KotlinLogging

/**
 * Retry utilities for external API calls with exponential backoff.
 * Provides robust error handling for transient failures.
 */
object RetryUtils {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * Executes a block with retry logic for external API calls.
     * 
     * @param maxAttempts Maximum number of retry attempts (default: 3)
     * @param initialDelayMs Initial delay between retries in milliseconds (default: 1000)
     * @param maxDelayMs Maximum delay between retries in milliseconds (default: 10000)
     * @param shouldRetry Predicate to determine if an exception should trigger a retry
     * @param block The suspend function to execute
     * @return The result of the block execution
     * @throws Exception The last exception if all retries fail
     */
    suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        shouldRetry: (Exception) -> Boolean = { exception ->
            // Retry on 5xx errors and rate limits (429)
            when (exception) {
                is ExternalApiException -> {
                    val statusCode = exception.statusCode
                    statusCode in 500..599 || statusCode == 429
                }
                else -> false
            }
        },
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var currentDelay = initialDelayMs
        
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt == maxAttempts - 1) {
                    logger.error(e) { "All retry attempts failed after $maxAttempts attempts" }
                    throw e
                }
                
                if (!shouldRetry(e)) {
                    logger.warn { "Not retrying due to non-retryable error: ${e.message}" }
                    throw e
                }
                
                logger.warn { "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms: ${e.message}" }
                delay(currentDelay)
                
                // Exponential backoff with max delay cap
                currentDelay = minOf(currentDelay * 2, maxDelayMs)
            }
        }
        
        throw lastException ?: IllegalStateException("Should not reach here")
    }
}

/**
 * Custom exception for external API errors with status code.
 */
class ExternalApiException(
    message: String,
    val statusCode: Int,
    val exchange: String? = null,
    val originalCode: String? = null
) : Exception(message)
