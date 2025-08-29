package com.github.nanachi357.services

import mu.KotlinLogging

/**
 * Credential service for handling API credentials
 * 
 * Reads API credentials from environment variables:
 * - BYBIT_API_KEY: API key for Bybit
 * - BYBIT_SECRET_KEY: Secret key for Bybit
 * 
 * This is a simple implementation suitable for development and testing.
 * For production, consider using secure credential storage solutions.
 */
object CredentialService {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * Get API secret key as ByteArray for secure memory handling
     * 
     * @return Secret key as ByteArray
     * @throws SecurityException if secret key cannot be retrieved
     */
    fun getSecretKey(): ByteArray {
        val secretKey = System.getenv("BYBIT_SECRET_KEY")
            ?: throw SecurityException("BYBIT_SECRET_KEY environment variable not set")
        
        if (secretKey.isBlank()) {
            throw SecurityException("BYBIT_SECRET_KEY environment variable is empty")
        }
        
        logger.debug { "Retrieved secret key from environment variables" }
        return secretKey.toByteArray()
    }
    
    /**
     * Get API key as String
     * 
     * @return API key
     * @throws SecurityException if API key cannot be retrieved
     */
    fun getApiKey(): String {
        val apiKey = System.getenv("BYBIT_API_KEY")
            ?: throw SecurityException("BYBIT_API_KEY environment variable not set")
        
        if (apiKey.isBlank()) {
            throw SecurityException("BYBIT_API_KEY environment variable is empty")
        }
        
        logger.debug { "Retrieved API key from environment variables" }
        return apiKey
    }
    
    /**
     * Clear any cached credentials from memory
     * 
     * This method is a placeholder for future implementations that might cache credentials.
     * Currently, environment variables cannot be cleared programmatically.
     */
    fun clearCredentials() {
        logger.debug { "Environment variables cannot be cleared programmatically" }
    }
}
