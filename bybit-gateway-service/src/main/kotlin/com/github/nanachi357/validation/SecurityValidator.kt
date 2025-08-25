package com.github.nanachi357.validation

import mu.KotlinLogging

/**
 * Security validator for input sanitization and validation
 */
object SecurityValidator {
    
    private val SYMBOL_PATTERN = Regex("^[A-Z0-9]+$") // Only format, no length check
    private val logger = KotlinLogging.logger {}
    
    // Regex patterns for validation
    private val ALPHANUMERIC_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
    private val URL_SAFE_PATTERN = Regex("^[a-zA-Z0-9/._-]+$")
    
    /**
     * Validates trading symbol format
     */
    fun validateSymbol(symbol: String): ValidationResult {
        return when {
            symbol.isBlank() -> ValidationResult.Error("Symbol cannot be blank")
            else -> {
                val uppercaseSymbol = symbol.uppercase()
                when {
                    uppercaseSymbol.length < 2 -> ValidationResult.Error("Symbol too short (min 2 characters)")
                    !SYMBOL_PATTERN.matches(uppercaseSymbol) -> ValidationResult.Error("Invalid symbol format. Use uppercase letters and numbers only")
                    uppercaseSymbol.length > 20 -> ValidationResult.Error("Symbol too long (max 20 characters)")
                    else -> ValidationResult.Success(uppercaseSymbol)
                }
            }
        }
    }
    
    /**
     * Validates list of symbols
     */
    fun validateSymbols(symbols: List<String>): ValidationResult {
        if (symbols.isEmpty()) {
            return ValidationResult.Error("At least one symbol is required")
        }
        
        if (symbols.size > 50) {
            return ValidationResult.Error("Too many symbols (max 50)")
        }
        
        val validatedSymbols = mutableListOf<String>()
        val errors = mutableListOf<String>()
        
        symbols.forEachIndexed { index, symbol ->
            when (val result = validateSymbol(symbol)) {
                is ValidationResult.Success<*> -> validatedSymbols.add(result.value as String)
                is ValidationResult.Error -> errors.add("Symbol $index: ${result.message}")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success(validatedSymbols)
        } else {
            ValidationResult.Error("Validation errors: ${errors.joinToString("; ")}")
        }
    }
    
    /**
     * Sanitizes and validates query parameters
     */
    fun sanitizeQueryParam(param: String?): String? {
        return param?.trim()?.takeIf { it.isNotBlank() }
    }
    
    /**
     * Validates alphanumeric input
     */
    fun validateAlphanumeric(input: String, fieldName: String): ValidationResult {
        return when {
            input.isBlank() -> ValidationResult.Error("$fieldName cannot be blank")
            input.length > 100 -> ValidationResult.Error("$fieldName too long (max 100 characters)")
            !ALPHANUMERIC_PATTERN.matches(input) -> ValidationResult.Error("Invalid $fieldName format")
            else -> ValidationResult.Success(input)
        }
    }
    
    /**
     * Logs potential security threats
     */
    fun logSecurityEvent(event: String, details: Map<String, String> = emptyMap()) {
        logger.warn { "Security event: $event, Details: $details" }
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    data class Success<T>(val value: T) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
