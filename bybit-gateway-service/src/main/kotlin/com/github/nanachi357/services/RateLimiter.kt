package com.github.nanachi357.services

import com.github.nanachi357.utils.SecureLoggingUtils
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rate limiter for API requests
 * 
 * Implements sliding window rate limiting to prevent abuse:
 * - Per-API key rate limiting
 * - Configurable limits (requests per minute/hour)
 * - Automatic cleanup of expired entries
 * - Thread-safe implementation
 */
class RateLimiter(
    private val requestsPerMinute: Int = 60,
    private val requestsPerHour: Int = 1000,
    private val cleanupIntervalMinutes: Int = 5
) {
    
    private val logger = KotlinLogging.logger {}
    private val requestCounts = ConcurrentHashMap<String, MutableList<Instant>>()
    private var lastCleanup = Instant.now()
    
    /**
     * Check if request is allowed for given API key
     * 
     * @param apiKey The API key to check
     * @return True if request is allowed
     */
    fun isAllowed(apiKey: String): Boolean {
        val now = Instant.now()
        
        // Cleanup old entries periodically
        if (shouldCleanup(now)) {
            cleanup(now)
        }
        
        val requests = requestCounts.computeIfAbsent(apiKey) { mutableListOf() }
        
        synchronized(requests) {
            // Remove requests older than 1 hour
            requests.removeAll { it.isBefore(now.minusSeconds(3600)) }
            
            // Check hourly limit
            if (requests.size >= requestsPerHour) {
                logger.warn { "Hourly rate limit exceeded for API key: ${maskApiKey(apiKey)}" }
                return false
            }
            
            // Count requests in last minute
            val recentRequests = requests.count { it.isAfter(now.minusSeconds(60)) }
            
            // Check per-minute limit
            if (recentRequests >= requestsPerMinute) {
                logger.warn { "Per-minute rate limit exceeded for API key: ${maskApiKey(apiKey)}" }
                return false
            }
            
            // Add current request
            requests.add(now)
            return true
        }
    }
    
    /**
     * Get current request count for API key
     * 
     * @param apiKey The API key
     * @return Current request counts (per minute and per hour)
     */
    fun getRequestCount(apiKey: String): RequestCount {
        val now = Instant.now()
        val requests = requestCounts[apiKey] ?: return RequestCount(0, 0)
        
        synchronized(requests) {
            val recentRequests = requests.count { it.isAfter(now.minusSeconds(60)) }
            val hourlyRequests = requests.count { it.isAfter(now.minusSeconds(3600)) }
            
            return RequestCount(recentRequests, hourlyRequests)
        }
    }
    
    /**
     * Reset rate limit for given API key
     * 
     * @param apiKey The API key to reset
     */
    fun reset(apiKey: String) {
        requestCounts.remove(apiKey)
        logger.info { "Rate limit reset for API key: ${maskApiKey(apiKey)}" }
    }
    
    /**
     * Get rate limit statistics
     * 
     * @return Rate limit statistics
     */
    fun getStatistics(): RateLimitStats {
        val now = Instant.now()
        val totalKeys = requestCounts.size
        val activeKeys = requestCounts.count { (_, requests) ->
            requests.any { it.isAfter(now.minusSeconds(3600)) }
        }
        
        return RateLimitStats(
            totalKeys = totalKeys,
            activeKeys = activeKeys,
            requestsPerMinute = requestsPerMinute,
            requestsPerHour = requestsPerHour
        )
    }
    
    private fun shouldCleanup(now: Instant): Boolean {
        return now.isAfter(lastCleanup.plusSeconds(cleanupIntervalMinutes * 60L))
    }
    
    private fun cleanup(now: Instant) {
        val cutoff = now.minusSeconds(3600) // Remove entries older than 1 hour
        
        requestCounts.entries.removeIf { (_, requests) ->
            synchronized(requests) {
                requests.removeAll { it.isBefore(cutoff) }
                requests.isEmpty()
            }
        }
        
        lastCleanup = now
        logger.debug { "Rate limiter cleanup completed" }
    }
    
    private fun maskApiKey(apiKey: String): String {
        return SecureLoggingUtils.maskApiKey(apiKey)
    }
}

/**
 * Request count information
 */
data class RequestCount(
    val perMinute: Int,
    val perHour: Int
)

/**
 * Rate limit statistics
 */
data class RateLimitStats(
    val totalKeys: Int,
    val activeKeys: Int,
    val requestsPerMinute: Int,
    val requestsPerHour: Int
)
