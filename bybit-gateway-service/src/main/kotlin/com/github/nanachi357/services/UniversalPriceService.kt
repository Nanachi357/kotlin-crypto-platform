package com.github.nanachi357.services

import com.github.nanachi357.clients.BybitApiClient
import com.github.nanachi357.models.exchange.*
import com.github.nanachi357.models.bybit.BybitTickerItem
import com.github.nanachi357.validation.SymbolValidator
import com.github.nanachi357.validation.SecurityValidator
import com.github.nanachi357.validation.ValidationResult
import com.github.nanachi357.utils.ResponseMapper
import mu.KotlinLogging

/**
 * Universal service layer for market data operations using new exchange abstraction.
 * 
 * Implements universal response format for multi-exchange support.
 */
class UniversalPriceService(private val bybitClient: BybitApiClient) {
    
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
                return ResponseMapper.error(
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
                return ResponseMapper.error(
                    error = exception.message ?: "Invalid format",
                    exchange = Exchange.BYBIT
                )
            }
        )
        
        return runCatching { 
            bybitClient.getMarketTicker(validatedSymbol, category) 
        }.fold(
            onSuccess = { bybitResponse ->
                val duration = System.currentTimeMillis() - startTime
                logger.info { "Market data fetched successfully: symbol=$validatedSymbol, category=$category, duration=${duration}ms" }
                
                // Log performance warning for slow requests
                if (duration > 2000) {
                    logger.warn { "Slow market data request: symbol=$validatedSymbol, duration=${duration}ms" }
                }
                
                // Transform to universal format
                if (bybitResponse.retCode == 0 && bybitResponse.result.list.isNotEmpty()) {
                    val tickerItem = bybitResponse.result.list.first()
                    val priceData = ResponseMapper.mapBybitTickerToPriceData(tickerItem)
                    ResponseMapper.success(
                        data = priceData,
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString(),
                        includeDebugInfo = false // Hide sensitive data in production
                    )
                } else {
                    ResponseMapper.error(
                        error = bybitResponse.retMsg,
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString()
                    )
                }
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Market data fetch failed: symbol=$validatedSymbol, category=$category, duration=${duration}ms" }
                ResponseMapper.error(
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
                return ResponseMapper.error(
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
            bybitClient.getMarketTickers(validatedSymbols, category) 
        }.fold(
            onSuccess = { bybitResponse ->
                val duration = System.currentTimeMillis() - startTime
                logger.info { "Batch market data fetched successfully: symbols=${validatedSymbols.size}, category=$category, duration=${duration}ms" }
                
                // Log performance warning for slow requests
                if (duration > 4000) {
                    logger.warn { "Slow batch market data request: symbols=${validatedSymbols.size}, duration=${duration}ms" }
                }
                
                // Transform to universal format
                if (bybitResponse.retCode == 0) {
                    val priceDataList = bybitResponse.result.list.map { tickerItem ->
                        ResponseMapper.mapBybitTickerToPriceData(tickerItem)
                    }
                    val batchResponse = BatchPriceResponse(
                        prices = priceDataList,
                        exchange = Exchange.BYBIT
                    )
                    ResponseMapper.success(
                        data = batchResponse,
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString(),
                        includeDebugInfo = false // Hide sensitive data in production
                    )
                } else {
                    ResponseMapper.error(
                        error = bybitResponse.retMsg,
                        exchange = Exchange.BYBIT,
                        originalResponse = bybitResponse.toString()
                    )
                }
            },
            onFailure = { exception ->
                val duration = System.currentTimeMillis() - startTime
                logger.error(exception) { "Batch market data fetch failed: symbols=${validatedSymbols.size}, category=$category, duration=${duration}ms" }
                ResponseMapper.error(
                    error = exception.message ?: "Unknown error",
                    exchange = Exchange.BYBIT
                )
            }
        )
    }
}
