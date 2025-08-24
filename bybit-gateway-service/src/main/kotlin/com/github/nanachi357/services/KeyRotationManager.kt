package com.github.nanachi357.services

import mu.KotlinLogging
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Key rotation manager for handling API key lifecycle
 * 
 * Manages key rotation, expiration, and fallback strategies:
 * - Primary and secondary API keys
 * - Automatic key rotation
 * - Graceful degradation during key changes
 * - Monitoring key expiration
 */
interface KeyRotationManager {
    
    /**
     * Get current active API key
     * 
     * @return Current active API key
     * @throws SecurityException if no valid key is available
     */
    fun getCurrentApiKey(): String
    
    /**
     * Get current active secret key
     * 
     * @return Current active secret key as ByteArray
     * @throws SecurityException if no valid key is available
     */
    fun getCurrentSecretKey(): ByteArray
    
    /**
     * Check if key rotation is needed
     * 
     * @return True if key rotation should be performed
     */
    fun isRotationNeeded(): Boolean
    
    /**
     * Perform key rotation
     * 
     * @return True if rotation was successful
     */
    fun rotateKeys(): Boolean
    
    /**
     * Get key expiration information
     * 
     * @return Key expiration details
     */
    fun getKeyExpirationInfo(): KeyExpirationInfo
}

/**
 * Key expiration information
 */
data class KeyExpirationInfo(
    val expiresAt: Instant,
    val isExpired: Boolean,
    val daysUntilExpiration: Long,
    val shouldRotate: Boolean
)

/**
 * Simple key rotation manager implementation
 * 
 * This is a basic implementation for development/testing.
 * Production should use more sophisticated key management systems.
 */
class SimpleKeyRotationManager(
    private val credentialManager: SecureCredentialManager,
    private val rotationThresholdDays: Long = 30
) : KeyRotationManager {
    
    private val logger = KotlinLogging.logger {}
    private var lastRotationCheck: Instant = Instant.now()
    
    override fun getCurrentApiKey(): String {
        return credentialManager.getApiKey()
    }
    
    override fun getCurrentSecretKey(): ByteArray {
        return credentialManager.getSecretKey()
    }
    
    override fun isRotationNeeded(): Boolean {
        // For simple implementation, we'll check periodically
        val now = Instant.now()
        val daysSinceLastCheck = ChronoUnit.DAYS.between(lastRotationCheck, now)
        
        if (daysSinceLastCheck >= rotationThresholdDays) {
            lastRotationCheck = now
            logger.warn { "Key rotation check triggered after $daysSinceLastCheck days" }
            return true
        }
        
        return false
    }
    
    override fun rotateKeys(): Boolean {
        logger.info { "Key rotation requested" }
        
        // For simple implementation, we'll just clear credentials
        // In production, this would trigger actual key rotation
        try {
            credentialManager.clearCredentials()
            logger.info { "Credentials cleared for rotation" }
            return true
        } catch (e: Exception) {
            logger.error(e) { "Failed to rotate keys" }
            return false
        }
    }
    
    override fun getKeyExpirationInfo(): KeyExpirationInfo {
        val now = Instant.now()
        val expirationDate = lastRotationCheck.plus(rotationThresholdDays, ChronoUnit.DAYS)
        val daysUntilExpiration = ChronoUnit.DAYS.between(now, expirationDate)
        val isExpired = now.isAfter(expirationDate)
        val shouldRotate = daysUntilExpiration <= 7 // Rotate if less than 7 days left
        
        return KeyExpirationInfo(
            expiresAt = expirationDate,
            isExpired = isExpired,
            daysUntilExpiration = daysUntilExpiration,
            shouldRotate = shouldRotate
        )
    }
}
