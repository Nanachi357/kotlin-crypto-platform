package com.github.nanachi357.services

import mu.KotlinLogging

/**
 * Production credential manager stub
 * 
 * This is a placeholder implementation for production environments.
 * In production, this should be replaced with:
 * - HashiCorp Vault integration
 * - AWS Secrets Manager integration
 * - Other secure credential storage solutions
 * 
 * TODO: Implement real production credential manager
 */
class ProductionCredentialManager : SecureCredentialManager {
    
    private val logger = KotlinLogging.logger {}
    
    override fun getSecretKey(): ByteArray {
        logger.error { "Production credential manager not implemented" }
        throw NotImplementedError(
            "Production credential manager not implemented. " +
            "Use HashiCorp Vault or AWS Secrets Manager for production environments."
        )
    }
    
    override fun getApiKey(): String {
        logger.error { "Production credential manager not implemented" }
        throw NotImplementedError(
            "Production credential manager not implemented. " +
            "Use HashiCorp Vault or AWS Secrets Manager for production environments."
        )
    }
    
    override fun clearCredentials() {
        // Nothing to clear in stub implementation
        logger.debug { "Production credential manager stub - nothing to clear" }
    }
}
