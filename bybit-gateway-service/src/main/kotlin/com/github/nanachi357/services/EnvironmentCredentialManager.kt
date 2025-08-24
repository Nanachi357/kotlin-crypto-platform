package com.github.nanachi357.services

import mu.KotlinLogging

/**
 * Environment-based credential manager for development and testing
 * 
 * Reads API credentials from environment variables:
 * - BYBIT_API_KEY: API key for Bybit
 * - BYBIT_SECRET_KEY: Secret key for Bybit
 * 
 * This implementation is suitable for development and testing environments
 * but should not be used in production due to security concerns.
 */
class EnvironmentCredentialManager : SecureCredentialManager {
    
    private val logger = KotlinLogging.logger {}
    
    override fun getSecretKey(): ByteArray {
        val secretKey = System.getenv("BYBIT_SECRET_KEY")
            ?: throw SecurityException("BYBIT_SECRET_KEY environment variable not set")
        
        if (secretKey.isBlank()) {
            throw SecurityException("BYBIT_SECRET_KEY environment variable is empty")
        }
        
        logger.debug { "Retrieved secret key from environment variables" }
        return secretKey.toByteArray()
    }
    
    override fun getApiKey(): String {
        val apiKey = System.getenv("BYBIT_API_KEY")
            ?: throw SecurityException("BYBIT_API_KEY environment variable not set")
        
        if (apiKey.isBlank()) {
            throw SecurityException("BYBIT_API_KEY environment variable is empty")
        }
        
        logger.debug { "Retrieved API key from environment variables" }
        return apiKey
    }
    
    override fun clearCredentials() {
        // Environment variables cannot be cleared programmatically
        // This is a limitation of this implementation
        logger.debug { "Environment variables cannot be cleared programmatically" }
    }
}
