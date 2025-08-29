package com.github.nanachi357.services

import com.github.nanachi357.models.exchange.*
import com.github.nanachi357.models.bybit.BybitTickerItem
import com.github.nanachi357.validation.SymbolValidator
import com.github.nanachi357.validation.SecurityValidator
import com.github.nanachi357.validation.ValidationResult
import com.github.nanachi357.utils.ResponseMapper
import com.github.nanachi357.exchanges.BybitExchangeService
import mu.KotlinLogging

/**
 * Universal service layer for market data operations using new exchange abstraction.
 * 
 * Implements universal response format for multi-exchange support.
 */
class UniversalPriceService {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * Gets market ticker data for a specific symbol using universal format.
     * 
     * @param symbol Trading pair symbol (e.g., "BTCUSDT")
     * @param category Market category (default: SPOT)
     * @return ExchangeResponse containing universal price data
     */
    suspend fun getPrice(
        symbol: String,
        category: com.github.nanachi357.models.MarketCategory = com.github.nanachi357.models.MarketCategory.SPOT
    ): ExchangeResponse<PriceData> {
        val startTime = System.currentTimeMillis()
        
        // Security validation first
        when (val securityValidation = SecurityValidator.validateSymbol(symbol)) {
            is ValidationResult.Success<*> -> {
                val validatedSymbol = securityValidation.value as String
                logger.info { "Security validation passed for symbol: $validatedSymbol" }
            }
            is ValidationResult.Error -> {
                SecurityValidator.logSecurityEvent("Invalid symbol input", mapOf("symbol" to symbol))
                return ResponseMapper.error<PriceData>(
                    error = securityValidation.message,
                    exchange = Exchange.BYBIT
                )
            }
        }
        
        // Validate symbol format (existing validation)
        val validatedSymbol = runCatching { 
            SymbolValidator.validateSymbol(symbol) 
        }.fold(
            onSuccess = { it },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn { "Symbol validation failed: symbol=$symbol, duration=${duration}ms, error=${exception.message}" }
                return ResponseMapper.error<PriceData>(
                    error = exception.message ?: "Invalid format",
                    exchange = Exchange.BYBIT
                )
            }
        )
        
        return runCatching { 
            BybitExchangeService.getPrice(validatedSymbol)
        }.fold(
            onSuccess = { bybitResponse ->
                val duration = System.currentTimeMillis() - startTime
                logger.info { "Market data fetched successfully: symbol=$validatedSymbol, category=$category, duration=${duration}ms" }
                
                // Log performance warning for slow requests
                if (duration > 2000) {
                    logger.warn { "Slow market data request: symbol=$validatedSymbol, duration=${duration}ms" }
                }
                
                // Transform to universal format
                if (bybitResponse.success && bybitResponse.data != null) {
                    val priceData = bybitResponse.data
                    ResponseMapper.success<PriceData>(
                        data = priceData,
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString(),
                        includeDebugInfo = false // Hide sensitive data in production
                    )
                } else {
                    ResponseMapper.error<PriceData>(
                        error = bybitResponse.error ?: "Unknown error",
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString()
                    )
                }
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Market data fetch failed: symbol=$validatedSymbol, category=$category, duration=${duration}ms" }
                ResponseMapper.error<PriceData>(
                    error = exception.message ?: "Unknown error",
                    exchange = Exchange.BYBIT
                )
            }
        )
    }
    
    /**
     * Gets market ticker data for multiple symbols using universal format.
     * 
     * @param symbols List of trading pair symbols (optional - if empty, returns all symbols)
     * @param category Market category (default: SPOT)
     * @return ExchangeResponse containing universal batch price data
     */
    suspend fun getPrices(
        symbols: List<String> = emptyList(),
        category: com.github.nanachi357.models.MarketCategory = com.github.nanachi357.models.MarketCategory.SPOT
    ): ExchangeResponse<BatchPriceResponse> {
        val startTime = System.currentTimeMillis()
        
        // Validate symbols with graceful degradation (filter out invalid ones)
        val validatedSymbols = if (symbols.isNotEmpty()) {
            val validSymbols = SymbolValidator.validateSymbolsGracefully(symbols)
            if (validSymbols.isEmpty()) {
                val duration = System.currentTimeMillis() - startTime
                logger.warn { "All symbols validation failed: symbols=$symbols, duration=${duration}ms" }
                return ResponseMapper.error<BatchPriceResponse>(
                    error = "No valid symbols provided. All symbols failed validation.",
                    exchange = Exchange.BYBIT
                )
            }
            if (validSymbols.size < symbols.size) {
                val filteredCount = symbols.size - validSymbols.size
                logger.info { "Filtered out $filteredCount invalid symbols from request" }
            }
            validSymbols
        } else {
            emptyList()
        }
        
        return runCatching { 
            BybitExchangeService.getPrices(validatedSymbols)
        }.fold(
            onSuccess = { bybitResponse ->
                val duration = System.currentTimeMillis() - startTime
                logger.info { "Batch market data fetched successfully: symbols=${validatedSymbols.size}, category=$category, duration=${duration}ms" }
                
                // Log performance warning for slow requests
                if (duration > 4000) {
                    logger.warn { "Slow batch market data request: symbols=${validatedSymbols.size}, duration=${duration}ms" }
                }
                
                // Transform to universal format
                if (bybitResponse.success && bybitResponse.data != null) {
                    val batchResponse = bybitResponse.data
                    ResponseMapper.success<BatchPriceResponse>(
                        data = batchResponse,
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString(),
                        includeDebugInfo = false // Hide sensitive data in production
                    )
                } else {
                    ResponseMapper.error<BatchPriceResponse>(
                        error = bybitResponse.error ?: "Unknown error",
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString()
                    )
                }
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Batch market data fetch failed: symbols=${validatedSymbols.size}, category=$category, duration=${duration}ms" }
                ResponseMapper.error<BatchPriceResponse>(
                    error = exception.message ?: "Unknown error",
                    exchange = Exchange.BYBIT
                )
            }
        )
    }
}
