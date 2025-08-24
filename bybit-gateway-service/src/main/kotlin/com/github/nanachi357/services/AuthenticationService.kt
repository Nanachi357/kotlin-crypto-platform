package com.github.nanachi357.services

import com.github.nanachi357.models.*
import com.github.nanachi357.utils.HmacSignatureGenerator
import com.github.nanachi357.utils.SecureLoggingUtils
import mu.KotlinLogging
import java.util.*
import java.nio.charset.StandardCharsets

/**
 * Service for handling Bybit API authentication
 * 
 * Manages HMAC signature generation, timestamp validation,
 * and authentication headers for private API endpoints.
 * 
 * Enhanced with:
 * - Nonce generation for replay protection
 * - Request signature validation
 * - Enhanced security measures
 */
class AuthenticationService(
    private val credentialManager: SecureCredentialManager,
    private val nonceManager: NonceManager = NonceManager(),
    private val signatureValidator: RequestSignatureValidator = RequestSignatureValidator(credentialManager, NonceManager())
) {
    
    private val logger = KotlinLogging.logger {}
    private val signatureGenerator = HmacSignatureGenerator()
    
    /**
     * Constructor with default environment credential manager
     */
    constructor() : this(EnvironmentCredentialManager())
    
    /**
     * Generate nonce for request replay protection
     * 
     * @return Unique nonce string
     */
    fun generateNonce(): String {
        return nonceManager.generateNonce()
    }
    
    /**
     * Validate incoming request signature
     * 
     * @param headers Authentication headers from request
     * @param queryParams Query parameters (for GET requests)
     * @param jsonBody JSON body (for POST requests)
     * @param nonce Request nonce for replay protection
     * @return Validation result with details
     */
    fun validateIncomingRequest(
        headers: BybitAuthHeaders,
        queryParams: Map<String, String>? = null,
        jsonBody: String? = null,
        nonce: String? = null
    ): SignatureValidationResult {
        return signatureValidator.validateRequestSignature(
            headers = headers,
            queryParams = queryParams,
            jsonBody = jsonBody,
            nonce = nonce
        )
    }
    
    /**
     * Verify data integrity of request payload
     * 
     * @param payload The payload to verify
     * @param signature The signature to validate against
     * @return True if data integrity is maintained
     */
    fun verifyDataIntegrity(payload: String, signature: String): Boolean {
        return signatureValidator.verifyDataIntegrity(payload, signature)
    }
    
    /**
     * Get nonce statistics
     * 
     * @return Nonce usage statistics
     */
    fun getNonceStatistics(): NonceStats {
        return nonceManager.getStatistics()
    }
    
    /**
     * Generate authentication headers for GET request
     * 
     * @param queryParams Query parameters to sign
     * @return Authentication headers map
     */
    fun generateGetAuthHeaders(
        queryParams: Map<String, String>
    ): BybitAuthHeaders {
        val timestamp = System.currentTimeMillis()
        
        // Get credentials from secure manager
        val apiKey = credentialManager.getApiKey()
        val secretKeyBytes = credentialManager.getSecretKey()
        
        // Add required authentication parameters
        val authParams = queryParams.toMutableMap()
        authParams["api_key"] = apiKey
        authParams["timestamp"] = timestamp.toString()
        authParams["recv_window"] = "5000"
        
        // SECURITY: Use CharArray for secure memory handling
        val secretKeyArray = String(secretKeyBytes, StandardCharsets.UTF_8).toCharArray()
        val signature = try {
            signatureGenerator.generateGetSignature(
                secretKeyArray,
                timestamp,
                authParams
            )
        } finally {
            // SECURITY: Clear secret key from memory
            secretKeyArray.fill('\u0000')
            // Clear the original byte array as well
            secretKeyBytes.fill(0)
        }
        
        logger.debug { SecureLoggingUtils.createAuthLogMessage("GET headers generated", apiKey) }
        
        return BybitAuthHeaders(
            apiKey = apiKey,
            timestamp = timestamp,
            signature = signature
        )
    }
    
    /**
     * Generate authentication headers for POST request
     * 
     * @param jsonBody JSON request body
     * @return Authentication headers map
     */
    fun generatePostAuthHeaders(
        jsonBody: String
    ): BybitAuthHeaders {
        val timestamp = System.currentTimeMillis()
        
        // Get credentials from secure manager
        val apiKey = credentialManager.getApiKey()
        val secretKeyBytes = credentialManager.getSecretKey()
        
        // SECURITY: Use CharArray for secure memory handling
        val secretKeyArray = String(secretKeyBytes, StandardCharsets.UTF_8).toCharArray()
        val signature = try {
            signatureGenerator.generatePostSignature(
                secretKeyArray,
                timestamp,
                jsonBody
            )
        } finally {
            // SECURITY: Clear secret key from memory
            secretKeyArray.fill('\u0000')
            // Clear the original byte array as well
            secretKeyBytes.fill(0)
        }
        
        logger.debug { SecureLoggingUtils.createAuthLogMessage("POST headers generated", apiKey) }
        
        return BybitAuthHeaders(
            apiKey = apiKey,
            timestamp = timestamp,
            signature = signature
        )
    }
    
    /**
     * Validate authentication response
     * 
     * @param response Response from Bybit API
     * @return True if authentication was successful
     */
    fun validateAuthResponse(response: AuthErrorResponse): Boolean {
        return when (response.retCode) {
            0 -> true // Success
            AuthErrorCodes.INVALID_API_KEY -> {
                logger.error { "Invalid API key: ${response.retMsg}" }
                false
            }
            AuthErrorCodes.INVALID_SIGNATURE -> {
                logger.error { "Invalid signature: ${response.retMsg}" }
                false
            }
            AuthErrorCodes.INVALID_TIMESTAMP -> {
                logger.error { "Invalid timestamp: ${response.retMsg}" }
                false
            }
            AuthErrorCodes.RATE_LIMIT_EXCEEDED -> {
                logger.warn { "Rate limit exceeded: ${response.retMsg}" }
                false
            }
            else -> {
                logger.error { "Authentication error: ${response.retCode} - ${response.retMsg}" }
                false
            }
        }
    }
    
    /**
     * Check if timestamp is within valid window
     * 
     * @param timestamp Request timestamp
     * @param recvWindow Receive window in milliseconds
     * @return True if timestamp is valid
     */
    fun isTimestampValid(timestamp: Long, recvWindow: Long = 5000): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDiff = Math.abs(currentTime - timestamp)
        
        logger.debug { "Timestamp validation: current=$currentTime, request=$timestamp, diff=${timeDiff}ms" }
        
        return timeDiff <= recvWindow
    }
    
    /**
     * Generate secure random string for testing
     * 
     * @param length Length of random string
     * @return Random string
     */
    fun generateRandomString(length: Int = 32): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
