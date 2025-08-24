package com.github.nanachi357.utils

import mu.KotlinLogging
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

/**
 * HMAC-SHA256 signature generator for Bybit API authentication
 * 
 * Implements the signature generation algorithm required by Bybit V5 API
 * for authenticated requests to private endpoints.
 */
class HmacSignatureGenerator {
    
    private val logger = KotlinLogging.logger {}
    
        /**
     * Generate HMAC-SHA256 signature for Bybit API request
     * 
     * @param secretKey The API secret key as CharArray for secure memory handling
     * @param message The message to sign (query string or request body)
     * @return Base64 encoded signature
     */
    fun generateSignature(secretKey: CharArray, message: String): String {
        require(secretKey.isNotEmpty()) { "Secret key cannot be empty" }
        require(message.isNotBlank()) { "Message cannot be empty or blank" }

        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secretKey.toString().toByteArray(), "HmacSHA256")
            mac.init(secretKeySpec)

            val hash = mac.doFinal(message.toByteArray())
            Base64.getEncoder().encodeToString(hash)

        } catch (e: Exception) {
            logger.error(e) { "Failed to generate HMAC signature" }
            throw SecurityException("Signature generation failed", e)
        } finally {
            // SECURITY: Clear secret key from memory
            secretKey.fill('\u0000')
        }
    }

    /**
     * Generate HMAC-SHA256 signature for Bybit API request (String version for backward compatibility)
     * 
     * @param secretKey The API secret key as String
     * @param message The message to sign (query string or request body)
     * @return Base64 encoded signature
     * @deprecated Use CharArray version for better security
     */
    @Deprecated("Use CharArray version for better security", ReplaceWith("generateSignature(secretKey.toCharArray(), message)"))
    fun generateSignature(secretKey: String, message: String): String {
        require(secretKey.isNotBlank()) { "Secret key cannot be empty or blank" }
        require(message.isNotBlank()) { "Message cannot be empty or blank" }

        val secretKeyArray = secretKey.toCharArray()
        return try {
            generateSignature(secretKeyArray, message)
        } finally {
            // SECURITY: Clear the converted CharArray
            secretKeyArray.fill('\u0000')
        }
    }
    
    /**
     * Generate signature for GET request with query parameters
     * 
     * @param secretKey The API secret key as CharArray for secure memory handling
     * @param timestamp Request timestamp
     * @param queryParams Map of query parameters
     * @return Base64 encoded signature
     */
    fun generateGetSignature(
        secretKey: CharArray, 
        timestamp: Long, 
        queryParams: Map<String, String>
    ): String {
        val sortedParams = queryParams.toSortedMap()
        val queryString = sortedParams.entries
            .joinToString("&") { "${it.key}=${it.value}" }
        
        logger.debug { "Signing GET request with ${queryParams.size} parameters" }
        return generateSignature(secretKey, queryString)
    }

    /**
     * Generate signature for GET request with query parameters (String version for backward compatibility)
     * 
     * @param secretKey The API secret key as String
     * @param timestamp Request timestamp
     * @param queryParams Map of query parameters
     * @return Base64 encoded signature
     * @deprecated Use CharArray version for better security
     */
    @Deprecated("Use CharArray version for better security", ReplaceWith("generateGetSignature(secretKey.toCharArray(), timestamp, queryParams)"))
    fun generateGetSignature(
        secretKey: String, 
        timestamp: Long, 
        queryParams: Map<String, String>
    ): String {
        val secretKeyArray = secretKey.toCharArray()
        return try {
            generateGetSignature(secretKeyArray, timestamp, queryParams)
        } finally {
            secretKeyArray.fill('\u0000')
        }
    }
    
    /**
     * Generate signature for POST request with JSON body
     * 
     * @param secretKey The API secret key as CharArray for secure memory handling
     * @param timestamp Request timestamp
     * @param jsonBody JSON request body
     * @return Base64 encoded signature
     */
    fun generatePostSignature(
        secretKey: CharArray, 
        timestamp: Long, 
        jsonBody: String
    ): String {
        val message = "$timestamp$jsonBody"
        logger.debug { "Signing POST request with timestamp $timestamp and ${jsonBody.length} chars body" }
        return generateSignature(secretKey, message)
    }

    /**
     * Generate signature for POST request with JSON body (String version for backward compatibility)
     * 
     * @param secretKey The API secret key as String
     * @param timestamp Request timestamp
     * @param jsonBody JSON request body
     * @return Base64 encoded signature
     * @deprecated Use CharArray version for better security
     */
    @Deprecated("Use CharArray version for better security", ReplaceWith("generatePostSignature(secretKey.toCharArray(), timestamp, jsonBody)"))
    fun generatePostSignature(
        secretKey: String, 
        timestamp: Long, 
        jsonBody: String
    ): String {
        val secretKeyArray = secretKey.toCharArray()
        return try {
            generatePostSignature(secretKeyArray, timestamp, jsonBody)
        } finally {
            secretKeyArray.fill('\u0000')
        }
    }
    
    /**
     * Validate signature against expected value
     * 
     * @param secretKey The API secret key as CharArray for secure memory handling
     * @param message The original message
     * @param expectedSignature The signature to validate
     * @return True if signature is valid
     */
    fun validateSignature(
        secretKey: CharArray, 
        message: String, 
        expectedSignature: String
    ): Boolean {
        val actualSignature = generateSignature(secretKey, message)
        return MessageDigest.isEqual(
            actualSignature.toByteArray(), 
            expectedSignature.toByteArray()
        )
    }

    /**
     * Validate signature against expected value (String version for backward compatibility)
     * 
     * @param secretKey The API secret key as String
     * @param message The original message
     * @param expectedSignature The signature to validate
     * @return True if signature is valid
     * @deprecated Use CharArray version for better security
     */
    @Deprecated("Use CharArray version for better security", ReplaceWith("validateSignature(secretKey.toCharArray(), message, expectedSignature)"))
    fun validateSignature(
        secretKey: String, 
        message: String, 
        expectedSignature: String
    ): Boolean {
        val secretKeyArray = secretKey.toCharArray()
        return try {
            validateSignature(secretKeyArray, message, expectedSignature)
        } finally {
            secretKeyArray.fill('\u0000')
        }
    }
}
