package com.github.nanachi357.validation

/**
 * Validator for Bybit trading pair symbols.
 * 
 * Implements basic validation rules for symbol format and structure.
 * Uses fail-fast approach for input validation before API calls.
 */
object SymbolValidator {
    
    /**
     * Minimum symbol length (e.g., "BTC")
     */
    private const val MIN_SYMBOL_LENGTH = 3
    
    /**
     * Maximum symbol length (e.g., "BTCUSDT")
     */
    private const val MAX_SYMBOL_LENGTH = 20
    
    /**
     * Regex pattern for valid symbol format: uppercase letters and numbers only
     */
    private val SYMBOL_PATTERN = Regex("^[A-Z0-9]+$")
    
    /**
     * Common quote currencies supported by Bybit
     */
    private val SUPPORTED_QUOTES = setOf(
        "USDT", "USDC", "BTC", "ETH", "BUSD", "DAI", "TUSD", "FRAX"
    )
    
    /**
     * Validates a trading pair symbol.
     * 
     * @param symbol The symbol to validate (e.g., "BTCUSDT")
     * @return The validated symbol in uppercase
     * @throws IllegalArgumentException if symbol is invalid
     */
    fun validateSymbol(symbol: String): String {
        // Basic null/empty check
        require(symbol.isNotBlank()) { 
            "Symbol cannot be blank or null" 
        }
        
        // Convert to uppercase for consistency
        val upperSymbol = symbol.uppercase()
        
        // Length validation
        require(upperSymbol.length in MIN_SYMBOL_LENGTH..MAX_SYMBOL_LENGTH) { 
            "Symbol length must be between $MIN_SYMBOL_LENGTH and $MAX_SYMBOL_LENGTH characters, got: ${upperSymbol.length}" 
        }
        
        // Format validation - only uppercase letters and numbers
        require(SYMBOL_PATTERN.matches(upperSymbol)) { 
            "Symbol must contain only uppercase letters and numbers, got: $upperSymbol" 
        }
        
        // Quote currency validation (optional - for better error messages)
        val quote = extractQuoteCurrency(upperSymbol)
        if (quote != null && !SUPPORTED_QUOTES.contains(quote)) {
            // Warning only - don't fail for unknown quotes
            println("Warning: Unknown quote currency: $quote in symbol: $upperSymbol")
        }
        
        return upperSymbol
    }
    
    /**
     * Validates a list of symbols.
     * 
     * @param symbols List of symbols to validate
     * @return List of validated symbols
     * @throws IllegalArgumentException if any symbol is invalid
     */
    fun validateSymbols(symbols: List<String>): List<String> {
        return symbols.map { validateSymbol(it) }
    }
    
    /**
     * Safely validates a symbol without throwing exceptions.
     * 
     * @param symbol The symbol to validate
     * @return Validated symbol or null if invalid
     */
    fun validateSymbolOrNull(symbol: String): String? {
        return runCatching { validateSymbol(symbol) }.getOrNull()
    }
    
    /**
     * Safely validates a list of symbols, filtering out invalid ones.
     * 
     * @param symbols List of symbols to validate
     * @return List of valid symbols only
     */
    fun validateSymbolsGracefully(symbols: List<String>): List<String> {
        return symbols.mapNotNull { validateSymbolOrNull(it) }
    }
    
    /**
     * Extracts quote currency from symbol (e.g., "USDT" from "BTCUSDT").
     * 
     * @param symbol The trading pair symbol
     * @return Quote currency or null if cannot be determined
     */
    private fun extractQuoteCurrency(symbol: String): String? {
        // Try to find known quote currencies at the end of symbol
        return SUPPORTED_QUOTES.find { quote ->
            symbol.endsWith(quote) && symbol.length > quote.length
        }
    }
    
    /**
     * Checks if a symbol is likely valid without strict validation.
     * 
     * @param symbol The symbol to check
     * @return true if symbol appears valid
     */
    fun isLikelyValid(symbol: String): Boolean {
        return runCatching { validateSymbol(symbol) }.isSuccess
    }
}
