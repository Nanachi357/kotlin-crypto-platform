package com.github.nanachi357.utils

/**
 * Secure logging utilities for masking sensitive data
 * 
 * Prevents sensitive information from being logged in plain text:
 * - API keys
 * - Secret keys
 * - Signatures
 * - Request bodies with sensitive data
 */
object SecureLoggingUtils {
    
    /**
     * Mask API key for secure logging
     * 
     * @param apiKey The API key to mask
     * @return Masked API key (first 4 + last 4 characters, rest masked)
     */
    fun maskApiKey(apiKey: String): String {
        if (apiKey.length <= 8) {
            return "*".repeat(apiKey.length)
        }
        return "${apiKey.take(4)}${"*".repeat(apiKey.length - 8)}${apiKey.takeLast(4)}"
    }
    
    /**
     * Mask secret key for secure logging
     * 
     * @param secretKey The secret key to mask
     * @return Masked secret key (first 2 + last 2 characters, rest masked)
     */
    fun maskSecretKey(secretKey: String): String {
        if (secretKey.length <= 4) {
            return "*".repeat(secretKey.length)
        }
        return "${secretKey.take(2)}${"*".repeat(secretKey.length - 4)}${secretKey.takeLast(2)}"
    }
    
    /**
     * Mask signature for secure logging
     * 
     * @param signature The signature to mask
     * @return Masked signature (first 8 + last 8 characters, rest masked)
     */
    fun maskSignature(signature: String): String {
        if (signature.length <= 16) {
            return "*".repeat(signature.length)
        }
        return "${signature.take(8)}${"*".repeat(signature.length - 16)}${signature.takeLast(8)}"
    }
    
    /**
     * Mask sensitive data in JSON request body
     * 
     * @param jsonBody The JSON body to mask
     * @return Masked JSON body with sensitive fields replaced
     */
    fun maskJsonBody(jsonBody: String): String {
        if (jsonBody.isBlank()) return jsonBody
        
        return jsonBody
            .replace(Regex("\"api_key\"\\s*:\\s*\"[^\"]*\""), "\"api_key\": \"***MASKED***\"")
            .replace(Regex("\"secret_key\"\\s*:\\s*\"[^\"]*\""), "\"secret_key\": \"***MASKED***\"")
            .replace(Regex("\"signature\"\\s*:\\s*\"[^\"]*\""), "\"signature\": \"***MASKED***\"")
            .replace(Regex("\"password\"\\s*:\\s*\"[^\"]*\""), "\"password\": \"***MASKED***\"")
            .replace(Regex("\"token\"\\s*:\\s*\"[^\"]*\""), "\"token\": \"***MASKED***\"")
    }
    
    /**
     * Mask sensitive data in query parameters
     * 
     * @param queryParams Map of query parameters
     * @return Masked query parameters map
     */
    fun maskQueryParams(queryParams: Map<String, String>): Map<String, String> {
        return queryParams.mapValues { (key, value) ->
            when (key.lowercase()) {
                "api_key", "apikey" -> maskApiKey(value)
                "secret_key", "secretkey" -> maskSecretKey(value)
                "signature", "sign" -> maskSignature(value)
                "password", "pass" -> "***MASKED***"
                "token" -> "***MASKED***"
                else -> value
            }
        }
    }
    
    /**
     * Create secure log message for authentication operations
     * 
     * @param operation The operation being performed
     * @param apiKey The API key (will be masked)
     * @param additionalInfo Additional information to log
     * @return Secure log message
     */
    fun createAuthLogMessage(operation: String, apiKey: String, additionalInfo: String = ""): String {
        val maskedKey = maskApiKey(apiKey)
        return "Auth $operation for API key: $maskedKey $additionalInfo".trim()
    }
    
    /**
     * Create secure log message for signature operations
     * 
     * @param operation The operation being performed
     * @param signature The signature (will be masked)
     * @param additionalInfo Additional information to log
     * @return Secure log message
     */
    fun createSignatureLogMessage(operation: String, signature: String, additionalInfo: String = ""): String {
        val maskedSignature = maskSignature(signature)
        return "Signature $operation: $maskedSignature $additionalInfo".trim()
    }
}
