package com.github.nanachi357.services

import com.github.nanachi357.models.BybitAuthHeaders
import com.github.nanachi357.utils.HmacSignatureGenerator
import com.github.nanachi357.utils.SecureLoggingUtils
import com.github.nanachi357.utils.RequestValidator
import com.github.nanachi357.services.CredentialService
import mu.KotlinLogging
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

/**
 * Request signature validator for incoming requests
 * 
 * Validates signatures of incoming requests to ensure:
 * - Request authenticity (not tampered with)
 * - Data integrity (payload hasn't been modified)
 * - Timestamp validity (not replay attacks)
 * - Nonce validation (one-time use tokens)
 */
class RequestSignatureValidator(
    private val nonceManager: NonceManager
) {
    
    private val logger = KotlinLogging.logger {}
    private val signatureGenerator = HmacSignatureGenerator()
    
    /**
     * Validate incoming request signature
     * 
     * @param headers Authentication headers from request
     * @param queryParams Query parameters (for GET requests)
     * @param jsonBody JSON body (for POST requests)
     * @param nonce Request nonce for replay protection
     * @return Validation result with details
     */
    fun validateRequestSignature(
        headers: BybitAuthHeaders,
        queryParams: Map<String, String>? = null,
        jsonBody: String? = null,
        nonce: String? = null
    ): SignatureValidationResult {
        
        try {
            // Step 1: Validate nonce (if provided)
            if (nonce != null && !nonceManager.validateNonce(nonce)) {
                logger.warn { "Invalid nonce provided: ${SecureLoggingUtils.maskApiKey(nonce)}" }
                return SignatureValidationResult(
                    isValid = false,
                    error = "Invalid or expired nonce",
                    errorCode = "INVALID_NONCE"
                )
            }
            
            // Step 2: Validate timestamp
            if (!isTimestampValid(headers.timestamp)) {
                logger.warn { "Invalid timestamp: ${headers.timestamp}" }
                return SignatureValidationResult(
                    isValid = false,
                    error = "Invalid timestamp",
                    errorCode = "INVALID_TIMESTAMP"
                )
            }
            
            // Step 3: Validate API key
            val expectedApiKey = CredentialService.getApiKey()
            if (headers.apiKey != expectedApiKey) {
                logger.warn { "Invalid API key: ${SecureLoggingUtils.maskApiKey(headers.apiKey)}" }
                return SignatureValidationResult(
                    isValid = false,
                    error = "Invalid API key",
                    errorCode = "INVALID_API_KEY"
                )
            }
            
            // Step 4: Generate expected signature
            val expectedSignature = generateExpectedSignature(
                queryParams = queryParams,
                jsonBody = jsonBody,
                timestamp = headers.timestamp
            )
            
            // Step 5: Compare signatures using constant-time comparison
            if (!constantTimeEquals(headers.signature, expectedSignature)) {
                logger.warn { "Signature mismatch for API key: ${SecureLoggingUtils.maskApiKey(headers.apiKey)}" }
                return SignatureValidationResult(
                    isValid = false,
                    error = "Invalid signature",
                    errorCode = "INVALID_SIGNATURE"
                )
            }
            
            // Step 6: Validate data integrity
            if (!validateDataIntegrity(queryParams, jsonBody)) {
                logger.warn { "Data integrity check failed for API key: ${SecureLoggingUtils.maskApiKey(headers.apiKey)}" }
                return SignatureValidationResult(
                    isValid = false,
                    error = "Data integrity validation failed",
                    errorCode = "DATA_INTEGRITY_FAILED"
                )
            }
            
            logger.debug { "Request signature validated successfully for API key: ${SecureLoggingUtils.maskApiKey(headers.apiKey)}" }
            
            return SignatureValidationResult(
                isValid = true,
                error = null,
                errorCode = null
            )
            
        } catch (e: Exception) {
            logger.error(e) { "Error during signature validation" }
            return SignatureValidationResult(
                isValid = false,
                error = "Internal validation error",
                errorCode = "VALIDATION_ERROR"
            )
        }
    }
    
    /**
     * Verify data integrity of request payload
     * 
     * @param payload The payload to verify
     * @param signature The signature to validate against
     * @return True if data integrity is maintained
     */
    fun verifyDataIntegrity(payload: String, signature: String): Boolean {
        try {
            val secretKeyBytes = CredentialService.getSecretKey()
            val secretKeyArray = String(secretKeyBytes, StandardCharsets.UTF_8).toCharArray()
            
            val expectedSignature = try {
                signatureGenerator.generateSignature(secretKeyArray, payload)
            } finally {
                secretKeyArray.fill('\u0000')
                secretKeyBytes.fill(0)
            }
            
            return constantTimeEquals(signature, expectedSignature)
            
        } catch (e: Exception) {
            logger.error(e) { "Error during data integrity verification" }
            return false
        }
    }
    
    /**
     * Validate request parameters for security
     * 
     * @param queryParams Query parameters to validate
     * @param jsonBody JSON body to validate
     * @return True if parameters are valid
     */
    fun validateRequestParameters(
        queryParams: Map<String, String>? = null,
        jsonBody: String? = null
    ): Boolean {
        
        // Validate query parameters
        if (queryParams != null) {
            val validationResult = RequestValidator.validateQueryParams(queryParams)
            if (!validationResult.isValid) {
                logger.warn { "Query parameters validation failed: ${validationResult.error}" }
                return false
            }
        }
        
        // Validate JSON body
        if (jsonBody != null) {
            val validationResult = RequestValidator.validateJsonBody(jsonBody)
            if (!validationResult.isValid) {
                logger.warn { "JSON body validation failed: ${validationResult.error}" }
                return false
            }
        }
        
        return true
    }
    
    /**
     * Generate expected signature for comparison
     * 
     * @param queryParams Query parameters (for GET requests)
     * @param jsonBody JSON body (for POST requests)
     * @param timestamp Request timestamp
     * @return Expected signature
     */
    private fun generateExpectedSignature(
        queryParams: Map<String, String>?,
        jsonBody: String?,
        timestamp: Long
    ): String {
        
        val secretKeyBytes = CredentialService.getSecretKey()
        val secretKeyArray = String(secretKeyBytes, StandardCharsets.UTF_8).toCharArray()
        
        return try {
            when {
                queryParams != null -> {
                    // GET request signature
                    signatureGenerator.generateGetSignature(
                        secretKeyArray,
                        timestamp,
                        queryParams
                    )
                }
                jsonBody != null -> {
                    // POST request signature
                    signatureGenerator.generatePostSignature(
                        secretKeyArray,
                        timestamp,
                        jsonBody
                    )
                }
                else -> {
                    throw IllegalArgumentException("Either queryParams or jsonBody must be provided")
                }
            }
        } finally {
            secretKeyArray.fill('\u0000')
            secretKeyBytes.fill(0)
        }
    }
    
    /**
     * Validate timestamp is within acceptable window
     * 
     * @param timestamp Request timestamp
     * @return True if timestamp is valid
     */
    private fun isTimestampValid(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDiff = Math.abs(currentTime - timestamp)
        val maxAllowedDiff = 5000L // 5 seconds tolerance
        
        return timeDiff <= maxAllowedDiff
    }
    
    /**
     * Validate data integrity of request
     * 
     * @param queryParams Query parameters
     * @param jsonBody JSON body
     * @return True if data integrity is maintained
     */
    private fun validateDataIntegrity(
        queryParams: Map<String, String>?,
        jsonBody: String?
    ): Boolean {
        
        // Additional integrity checks can be added here
        // For now, we rely on signature validation
        
        return true
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     * 
     * @param a First string
     * @param b Second string
     * @return True if strings are equal
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        return MessageDigest.isEqual(
            a.toByteArray(StandardCharsets.UTF_8),
            b.toByteArray(StandardCharsets.UTF_8)
        )
    }
}

/**
 * Result of signature validation
 */
data class SignatureValidationResult(
    val isValid: Boolean,
    val error: String?,
    val errorCode: String?
)
