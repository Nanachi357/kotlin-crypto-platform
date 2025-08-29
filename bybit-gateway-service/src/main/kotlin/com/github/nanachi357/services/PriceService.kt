package com.github.nanachi357.services

import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.MarketCategory
import com.github.nanachi357.models.ErrorResponseFactory
import com.github.nanachi357.models.bybit.BybitResponse
import com.github.nanachi357.models.bybit.BybitTickerResult
import com.github.nanachi357.models.exchange.*
import com.github.nanachi357.validation.SymbolValidator
import com.github.nanachi357.exchanges.BybitExchangeService
import mu.KotlinLogging

/**
 * Service layer for market data operations with functional error handling.
 * 
 * Implements graceful degradation pattern for non-critical market data operations.
 * Uses runCatching for safe execution and consistent error handling.
 */
class PriceService {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * Gets market ticker data for a specific symbol with functional error handling.
     * 
     * @param symbol Trading pair symbol (e.g., "BTCUSDT")
     * @param category Market category (default: SPOT)
     * @return ApiResponse containing either success data or error information
     */
    suspend fun getMarketTicker(
        symbol: String,
        category: MarketCategory = MarketCategory.SPOT
    ): ApiResponse<ExchangeResponse<PriceData>> {
        val startTime = System.currentTimeMillis()
        
        // Validate symbol first (fail-fast for input validation)
        val validatedSymbol = runCatching { 
            SymbolValidator.validateSymbol(symbol) 
        }.fold(
            onSuccess = { it },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn { "Symbol validation failed: symbol=$symbol, duration=${duration}ms, error=${exception.message}" }
                return ApiResponse.Error(
                    message = "Invalid symbol: ${exception.message ?: "Invalid format"}",
                    code = "VALIDATION_ERROR"
                )
            }
        )
        
        return runCatching { 
            BybitExchangeService.getPrice(validatedSymbol)
        }.fold(
            onSuccess = { 
                val duration = System.currentTimeMillis() - startTime
                logger.info { "Market data fetched successfully: symbol=$validatedSymbol, category=$category, duration=${duration}ms" }
                
                // Log performance warning for slow requests
                if (duration > 2000) {
                    logger.warn { "Slow market data request: symbol=$validatedSymbol, duration=${duration}ms" }
                }
                
                ApiResponse.Success(it)
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Market data fetch failed: symbol=$validatedSymbol, category=$category, duration=${duration}ms" }
                ApiResponse.Error(
                    message = "Market data error for $validatedSymbol: ${exception.message ?: "Unknown error"}",
                    code = "MARKET_DATA_ERROR"
                )
            }
        )
    }
    
    /**
     * Gets market ticker data for multiple symbols with functional error handling.
     * 
     * @param symbols List of trading pair symbols (optional - if empty, returns all symbols)
     * @param category Market category (default: SPOT)
     * @return ApiResponse containing either success data or error information
     */
    suspend fun getMarketTickers(
        symbols: List<String> = emptyList(),
        category: MarketCategory = MarketCategory.SPOT
    ): ApiResponse<ExchangeResponse<BatchPriceResponse>> {
        val startTime = System.currentTimeMillis()
        
        // Validate symbols with graceful degradation (filter out invalid ones)
        val validatedSymbols = if (symbols.isNotEmpty()) {
            val validSymbols = SymbolValidator.validateSymbolsGracefully(symbols)
            if (validSymbols.isEmpty()) {
                val duration = System.currentTimeMillis() - startTime
                logger.warn { "All symbols validation failed: symbols=$symbols, duration=${duration}ms" }
                return ApiResponse.Error(
                    message = "No valid symbols provided. All symbols failed validation.",
                    code = "VALIDATION_ERROR"
                )
            }
            if (validSymbols.size < symbols.size) {
                val filteredCount = symbols.size - validSymbols.size
                logger.warn { "Symbols filtered: original=${symbols.size}, valid=${validSymbols.size}, filtered=$filteredCount" }
            }
            validSymbols
        } else {
            emptyList() // Empty list means get all symbols
        }
        
        return runCatching { 
            BybitExchangeService.getPrices(validatedSymbols)
        }.fold(
            onSuccess = { 
                val duration = System.currentTimeMillis() - startTime
                val symbolCount = if (validatedSymbols.isEmpty()) "all symbols" else "${validatedSymbols.size} symbols"
                logger.info { "Market data fetched successfully: symbols=$symbolCount, category=$category, duration=${duration}ms" }
                
                // Log performance warning for slow requests
                if (duration > 3000) {
                    logger.warn { "Slow market data request: symbols=$symbolCount, duration=${duration}ms" }
                }
                
                ApiResponse.Success(it)
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Market data fetch failed: symbols=${validatedSymbols.size}, category=$category, duration=${duration}ms" }
                ApiResponse.Error(
                    message = "Market data error for multiple symbols: ${exception.message ?: "Unknown error"}",
                    code = "MARKET_DATA_ERROR"
                )
            }
        )
    }
    
    /**
     * Gets Bybit server time for testing API connectivity.
     * 
     * @return ApiResponse containing either server time or error information
     */
    suspend fun getServerTime(): ApiResponse<ExchangeResponse<Long>> {
        val startTime = System.currentTimeMillis()
        
        return runCatching { 
            BybitExchangeService.getServerTime()
        }.fold(
            onSuccess = { 
                val duration = System.currentTimeMillis() - startTime
                logger.info { "Server time fetched successfully: duration=${duration}ms" }
                ApiResponse.Success(it)
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Server time fetch failed: duration=${duration}ms" }
                ApiResponse.Error(
                    message = "Server time error: ${exception.message ?: "Unknown error"}",
                    code = "SERVER_TIME_ERROR"
                )
            }
        )
    }
}
