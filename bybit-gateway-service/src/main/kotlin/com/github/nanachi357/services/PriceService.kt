package com.github.nanachi357.services

import com.github.nanachi357.clients.BybitApiClient
import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.ErrorResponseFactory
import com.github.nanachi357.models.bybit.BybitResponse
import com.github.nanachi357.models.bybit.BybitTickerResult
import com.github.nanachi357.validation.SymbolValidator
import mu.KotlinLogging

/**
 * Service layer for market data operations with functional error handling.
 * 
 * Implements graceful degradation pattern for non-critical market data operations.
 * Uses runCatching for safe execution and consistent error handling.
 */
class PriceService(private val bybitClient: BybitApiClient) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * Gets market ticker data for a specific symbol with functional error handling.
     * 
     * @param symbol Trading pair symbol (e.g., "BTCUSDT")
     * @param category Market category (default: "spot")
     * @return ApiResponse containing either success data or error information
     */
    suspend fun getMarketTicker(
        symbol: String,
        category: String = "spot"
    ): ApiResponse<BybitResponse<BybitTickerResult>> {
        // Validate symbol first (fail-fast for input validation)
        val validatedSymbol = runCatching { 
            SymbolValidator.validateSymbol(symbol) 
        }.fold(
            onSuccess = { it },
            onFailure = { exception ->
                return ErrorResponseFactory.validationError(
                    field = "symbol",
                    message = exception.message ?: "Invalid format"
                )
            }
        )
        
        return runCatching { 
            bybitClient.getMarketTicker(validatedSymbol, category) 
        }.fold(
            onSuccess = { 
                // Log success for monitoring
                logger.info { "Successfully fetched market data for $validatedSymbol" }
                ApiResponse.Success(it)
            },
            onFailure = { exception ->
                // Log error for debugging
                logger.error(exception) { "Failed to fetch market data for $validatedSymbol" }
                ErrorResponseFactory.marketDataError(
                    symbol = validatedSymbol,
                    message = exception.message ?: "Unknown error"
                )
            }
        )
    }
    
    /**
     * Gets market ticker data for multiple symbols with functional error handling.
     * 
     * @param symbols List of trading pair symbols (optional - if empty, returns all symbols)
     * @param category Market category (default: "spot")
     * @return ApiResponse containing either success data or error information
     */
    suspend fun getMarketTickers(
        symbols: List<String> = emptyList(),
        category: String = "spot"
    ): ApiResponse<BybitResponse<BybitTickerResult>> {
        // Validate symbols with graceful degradation (filter out invalid ones)
        val validatedSymbols = if (symbols.isNotEmpty()) {
            val validSymbols = SymbolValidator.validateSymbolsGracefully(symbols)
            if (validSymbols.isEmpty()) {
                return ErrorResponseFactory.validationError(
                    field = "symbols",
                    message = "No valid symbols provided. All symbols failed validation."
                )
            }
            if (validSymbols.size < symbols.size) {
                println("Warning: Filtered out ${symbols.size - validSymbols.size} invalid symbols")
            }
            validSymbols
        } else {
            emptyList() // Empty list means get all symbols
        }
        
        return runCatching { 
            bybitClient.getMarketTickers(validatedSymbols, category) 
        }.fold(
            onSuccess = { 
                // Log success for monitoring
                val symbolCount = if (validatedSymbols.isEmpty()) "all symbols" else "${validatedSymbols.size} symbols"
                println("Successfully fetched market data for $symbolCount")
                ApiResponse.Success(it)
            },
            onFailure = { exception ->
                // Log error for debugging
                println("Failed to fetch market data: ${exception.message}")
                ErrorResponseFactory.marketDataError(
                    symbol = "multiple",
                    message = exception.message ?: "Unknown error"
                )
            }
        )
    }
    
    /**
     * Gets Bybit server time for testing API connectivity.
     * 
     * @return ApiResponse containing either server time or error information
     */
    suspend fun getServerTime(): ApiResponse<BybitResponse<com.github.nanachi357.models.bybit.BybitTimeResult>> {
        return runCatching { 
            bybitClient.getServerTime() 
        }.fold(
            onSuccess = { 
                println("Successfully fetched server time")
                ApiResponse.Success(it)
            },
            onFailure = { exception ->
                println("Failed to fetch server time: ${exception.message}")
                ErrorResponseFactory.serverTimeError(
                    message = exception.message ?: "Unknown error"
                )
            }
        )
    }
}
