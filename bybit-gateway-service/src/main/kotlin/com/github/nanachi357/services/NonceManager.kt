package com.github.nanachi357.services

import com.github.nanachi357.utils.SecureLoggingUtils
import mu.KotlinLogging
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Nonce manager for preventing replay attacks
 * 
 * Generates unique nonces for each request and validates them:
 * - Cryptographically secure random nonce generation
 * - Automatic cleanup of expired nonces
 * - Thread-safe implementation
 * - Configurable expiration time
 */
class NonceManager(
    private val nonceExpirationMinutes: Int = 5,
    private val cleanupIntervalMinutes: Int = 1
) {
    
    private val logger = KotlinLogging.logger {}
    private val secureRandom = SecureRandom()
    private val usedNonces = ConcurrentHashMap<String, Instant>()
    private val cleanupExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    
    init {
        // Start cleanup task
        cleanupExecutor.scheduleAtFixedRate(
            { cleanupExpiredNonces() },
            cleanupIntervalMinutes.toLong(),
            cleanupIntervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
        
        logger.info { "NonceManager initialized with ${nonceExpirationMinutes}min expiration" }
    }
    
    /**
     * Generate a unique nonce for request
     * 
     * @return Unique nonce string
     */
    fun generateNonce(): String {
        val nonce = generateSecureNonce()
        
        // Store with current timestamp
        usedNonces[nonce] = Instant.now()
        
        logger.debug { "Generated nonce: ${maskNonce(nonce)}" }
        return nonce
    }
    
    /**
     * Validate nonce and check for replay attacks
     * 
     * @param nonce The nonce to validate
     * @return True if nonce is valid and not a replay
     */
    fun validateNonce(nonce: String): Boolean {
        if (nonce.isBlank()) {
            logger.warn { "Empty nonce provided" }
            return false
        }
        
        val timestamp = usedNonces[nonce]
        if (timestamp == null) {
            logger.warn { "Nonce not found: ${maskNonce(nonce)}" }
            return false
        }
        
        // Check if nonce is expired
        val now = Instant.now()
        val expirationTime = timestamp.plusSeconds(nonceExpirationMinutes * 60L)
        
        if (now.isAfter(expirationTime)) {
            logger.warn { "Nonce expired: ${maskNonce(nonce)}" }
            usedNonces.remove(nonce)
            return false
        }
        
        // Remove nonce after first use (one-time use)
        usedNonces.remove(nonce)
        logger.debug { "Nonce validated and consumed: ${maskNonce(nonce)}" }
        
        return true
    }
    
    /**
     * Check if nonce is a replay attack
     * 
     * @param nonce The nonce to check
     * @return True if this appears to be a replay attack
     */
    fun isReplayAttack(nonce: String): Boolean {
        // If nonce is not in our map, it might be a replay
        // (assuming it was already used and removed)
        return !usedNonces.containsKey(nonce) && nonce.isNotBlank()
    }
    
    /**
     * Get nonce statistics
     * 
     * @return Nonce usage statistics
     */
    fun getStatistics(): NonceStats {
        val now = Instant.now()
        val activeNonces = usedNonces.count { (_, timestamp) ->
            val expirationTime = timestamp.plusSeconds(nonceExpirationMinutes * 60L)
            now.isBefore(expirationTime)
        }
        
        return NonceStats(
            totalNonces = usedNonces.size,
            activeNonces = activeNonces,
            expirationMinutes = nonceExpirationMinutes
        )
    }
    
    /**
     * Shutdown nonce manager and cleanup resources
     */
    fun shutdown() {
        cleanupExecutor.shutdown()
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            cleanupExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        
        usedNonces.clear()
        logger.info { "NonceManager shutdown completed" }
    }
    
    /**
     * Generate cryptographically secure nonce
     * 
     * @return Secure random nonce
     */
    private fun generateSecureNonce(): String {
        val bytes = ByteArray(32) // 256 bits
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Cleanup expired nonces
     */
    private fun cleanupExpiredNonces() {
        val now = Instant.now()
        val cutoff = now.minusSeconds(nonceExpirationMinutes * 60L)
        
        val removedCount = usedNonces.entries.removeIf { (_, timestamp) ->
            timestamp.isBefore(cutoff)
        }
        
        if (removedCount) {
            logger.debug { "Cleaned up expired nonces" }
        }
    }
    
    /**
     * Mask nonce for logging
     * 
     * @param nonce The nonce to mask
     * @return Masked nonce string
     */
    private fun maskNonce(nonce: String): String {
        return SecureLoggingUtils.maskApiKey(nonce)
    }
}

/**
 * Nonce usage statistics
 */
data class NonceStats(
    val totalNonces: Int,
    val activeNonces: Int,
    val expirationMinutes: Int
)
