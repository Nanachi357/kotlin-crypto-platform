package com.github.nanachi357.utils

import mu.KotlinLogging
import java.util.regex.Pattern

/**
 * Request validator for security and data integrity
 * 
 * Validates and sanitizes incoming requests:
 * - Input validation
 * - SQL injection prevention
 * - XSS prevention
 * - Parameter sanitization
 * - Size limits enforcement
 */
object RequestValidator {
    
    private val logger = KotlinLogging.logger {}
    
    // Patterns for validation
    private val SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT).*",
        Pattern.CASE_INSENSITIVE
    )
    
    private val XSS_PATTERN = Pattern.compile(
        ".*(<script|javascript:|vbscript:|onload=|onerror=|onclick=).*",
        Pattern.CASE_INSENSITIVE
    )
    
    private val API_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9]{20,}$")
    private val SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9/]{2,20}$")
    private val CATEGORY_PATTERN = Pattern.compile("^(spot|linear|inverse)$")
    
    // Size limits
    private const val MAX_QUERY_PARAMS = 50
    private const val MAX_PARAM_VALUE_LENGTH = 1000
    private const val MAX_JSON_BODY_SIZE = 10000
    
    /**
     * Validate API key format
     * 
     * @param apiKey The API key to validate
     * @return True if valid
     */
    fun isValidApiKey(apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        return API_KEY_PATTERN.matcher(apiKey).matches()
    }
    
    /**
     * Validate trading symbol format
     * 
     * @param symbol The symbol to validate
     * @return True if valid
     */
    fun isValidSymbol(symbol: String): Boolean {
        if (symbol.isBlank()) return false
        return SYMBOL_PATTERN.matcher(symbol).matches()
    }
    
    /**
     * Validate category parameter
     * 
     * @param category The category to validate
     * @return True if valid
     */
    fun isValidCategory(category: String): Boolean {
        if (category.isBlank()) return false
        return CATEGORY_PATTERN.matcher(category).matches()
    }
    
    /**
     * Validate query parameters
     * 
     * @param params Map of query parameters
     * @return Validation result
     */
    fun validateQueryParams(params: Map<String, String>): ValidationResult {
        // Check parameter count
        if (params.size > MAX_QUERY_PARAMS) {
            return ValidationResult(
                isValid = false,
                error = "Too many query parameters: ${params.size} (max: $MAX_QUERY_PARAMS)"
            )
        }
        
        // Validate each parameter
        for ((key, value) in params) {
            val keyValidation = validateParameterName(key)
            if (!keyValidation.isValid) {
                return keyValidation
            }
            
            val valueValidation = validateParameterValue(value)
            if (!valueValidation.isValid) {
                return valueValidation
            }
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validate JSON request body
     * 
     * @param jsonBody The JSON body to validate
     * @return Validation result
     */
    fun validateJsonBody(jsonBody: String): ValidationResult {
        if (jsonBody.length > MAX_JSON_BODY_SIZE) {
            return ValidationResult(
                isValid = false,
                error = "JSON body too large: ${jsonBody.length} bytes (max: $MAX_JSON_BODY_SIZE)"
            )
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(jsonBody).matches()) {
            logger.warn { "Potential SQL injection detected in JSON body" }
            return ValidationResult(
                isValid = false,
                error = "Invalid content detected in request body"
            )
        }
        
        // Check for XSS patterns
        if (XSS_PATTERN.matcher(jsonBody).matches()) {
            logger.warn { "Potential XSS detected in JSON body" }
            return ValidationResult(
                isValid = false,
                error = "Invalid content detected in request body"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Sanitize parameter value
     * 
     * @param value The value to sanitize
     * @return Sanitized value
     */
    fun sanitizeParameterValue(value: String): String {
        return value
            .trim()
            .replace(Regex("[<>\"']"), "") // Remove potentially dangerous characters
            .take(MAX_PARAM_VALUE_LENGTH) // Limit length
    }
    
    /**
     * Validate timestamp
     * 
     * @param timestamp The timestamp to validate
     * @param maxAgeSeconds Maximum age in seconds
     * @return Validation result
     */
    fun validateTimestamp(timestamp: Long, maxAgeSeconds: Long = 300): ValidationResult {
        val now = System.currentTimeMillis()
        val age = Math.abs(now - timestamp)
        
        if (age > maxAgeSeconds * 1000) {
            return ValidationResult(
                isValid = false,
                error = "Timestamp too old: ${age / 1000}s (max: ${maxAgeSeconds}s)"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validate recv_window parameter
     * 
     * @param recvWindow The recv_window value
     * @return Validation result
     */
    fun validateRecvWindow(recvWindow: Long): ValidationResult {
        if (recvWindow < 1 || recvWindow > 60000) {
            return ValidationResult(
                isValid = false,
                error = "Invalid recv_window: $recvWindow (must be 1-60000)"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    private fun validateParameterName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(
                isValid = false,
                error = "Parameter name cannot be blank"
            )
        }
        
        if (name.length > 50) {
            return ValidationResult(
                isValid = false,
                error = "Parameter name too long: ${name.length} (max: 50)"
            )
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(name).matches()) {
            logger.warn { "Potential SQL injection detected in parameter name: $name" }
            return ValidationResult(
                isValid = false,
                error = "Invalid parameter name"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    private fun validateParameterValue(value: String): ValidationResult {
        if (value.length > MAX_PARAM_VALUE_LENGTH) {
            return ValidationResult(
                isValid = false,
                error = "Parameter value too long: ${value.length} (max: $MAX_PARAM_VALUE_LENGTH)"
            )
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(value).matches()) {
            logger.warn { "Potential SQL injection detected in parameter value" }
            return ValidationResult(
                isValid = false,
                error = "Invalid parameter value"
            )
        }
        
        // Check for XSS patterns
        if (XSS_PATTERN.matcher(value).matches()) {
            logger.warn { "Potential XSS detected in parameter value" }
            return ValidationResult(
                isValid = false,
                error = "Invalid parameter value"
            )
        }
        
        return ValidationResult(isValid = true)
    }
}

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val error: String? = null
)
