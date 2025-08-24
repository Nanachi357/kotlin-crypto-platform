package com.github.nanachi357.services

/**
 * Secure credential manager interface for handling API credentials
 * 
 * Provides abstraction for different credential storage mechanisms:
 * - Environment variables (development/testing)
 * - HashiCorp Vault (production)
 * - AWS Secrets Manager (production)
 * - Other secure storage solutions
 */
interface SecureCredentialManager {
    
    /**
     * Get API secret key as ByteArray for secure memory handling
     * 
     * @return Secret key as ByteArray
     * @throws SecurityException if secret key cannot be retrieved
     */
    fun getSecretKey(): ByteArray
    
    /**
     * Get API key as String
     * 
     * @return API key
     * @throws SecurityException if API key cannot be retrieved
     */
    fun getApiKey(): String
    
    /**
     * Clear any cached credentials from memory
     * 
     * This method should be called when credentials are no longer needed
     * to ensure they are not persisted in memory longer than necessary.
     */
    fun clearCredentials() {
        // Default implementation does nothing
        // Override in implementations that cache credentials
    }
}
