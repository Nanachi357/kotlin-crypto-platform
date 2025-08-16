package com.github.nanachi357.services

import com.github.nanachi357.clients.BybitApiClient
import com.github.nanachi357.models.ApiResponse
import com.github.nanachi357.models.MarketCategory
import com.github.nanachi357.models.bybit.BybitResponse
import com.github.nanachi357.models.bybit.BybitTickerResult
import com.github.nanachi357.models.bybit.BybitTickerItem
import com.github.nanachi357.validation.SymbolValidator
import com.github.nanachi357.models.ErrorResponseFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging

/**
 * Service for efficient batch processing of multiple symbols with domain categories.
 * 
 * Implements parallel processing strategies and batch API optimization.
 * Supports different market categories (SPOT, LINEAR, INVERSE, OPTION).
 */
class BatchPriceService(private val bybitClient: BybitApiClient) {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        private const val DEFAULT_MAX_CONCURRENCY = 10
        private const val BATCH_THRESHOLD = 3 // Use batch API for 3+ symbols
    }
    
    /**
     * Gets prices for multiple symbols using optimal strategy based on count.
     * 
     * @param symbols List of trading pair symbols
     * @param category Market category (default: SPOT)
     * @param maxConcurrency Maximum concurrent requests (default: 10)
     * @return BatchPriceResult with successful prices, errors, and performance metrics
     */
    suspend fun getBatchPrices(
        symbols: List<String>,
        category: MarketCategory = MarketCategory.SPOT,
        maxConcurrency: Int = DEFAULT_MAX_CONCURRENCY
    ): BatchPriceResult {
        val startTime = System.currentTimeMillis()
        
        // Validate symbols with graceful degradation
        val validSymbols = SymbolValidator.validateSymbolsGracefully(symbols)
        if (validSymbols.isEmpty()) {
            val duration = System.currentTimeMillis() - startTime
            logger.warn { "Batch processing failed: no valid symbols, original=${symbols.size}, duration=${duration}ms" }
            return BatchPriceResult(
                successful = emptyList(),
                notFound = emptyList(),
                errors = mapOf("validation" to "No valid symbols provided"),
                requestTimeMs = duration,
                strategy = "none",
                category = category
            )
        }
        
        // Log validation results
        if (validSymbols.size < symbols.size) {
            val filteredCount = symbols.size - validSymbols.size
            logger.info { "Batch processing validation: original=${symbols.size}, valid=${validSymbols.size}, filtered=$filteredCount" }
        }
        
        // Choose optimal strategy based on symbol count
        val strategy = when {
            validSymbols.size == 1 -> "single"
            validSymbols.size < BATCH_THRESHOLD -> "parallel"
            else -> "batch"
        }
        
        logger.info { "Batch processing started: symbols=${validSymbols.size}, strategy=$strategy, category=$category" }
        
        val result = when {
            validSymbols.size == 1 -> getSinglePrice(validSymbols.first(), category)
            validSymbols.size < BATCH_THRESHOLD -> getParallelPrices(validSymbols, category, maxConcurrency)
            else -> getBatchApiPrices(validSymbols, category)
        }
        
        val totalDuration = System.currentTimeMillis() - startTime
        logger.info { "Batch processing completed: symbols=${validSymbols.size}, successful=${result.successful.size}, duration=${totalDuration}ms, strategy=${result.strategy}" }
        
        // Log performance warning for slow batch processing
        if (totalDuration > 5000) {
            logger.warn { "Slow batch processing: symbols=${validSymbols.size}, duration=${totalDuration}ms, strategy=${result.strategy}" }
        }
        
        return result
    }
    
    /**
     * Single symbol - direct API call
     */
    private suspend fun getSinglePrice(
        symbol: String,
        category: MarketCategory
    ): BatchPriceResult {
        val startTime = System.currentTimeMillis()
        try {
            val response = bybitClient.getMarketTicker(symbol, category)
            val requestTimeMs = System.currentTimeMillis() - startTime
            
            if (response.retCode == 0 && response.result.list.isNotEmpty()) {
                return BatchPriceResult(
                    successful = response.result.list,
                    notFound = emptyList(),
                    errors = emptyMap(),
                    requestTimeMs = requestTimeMs,
                    strategy = "single",
                    category = category
                )
            } else {
                return BatchPriceResult(
                    successful = emptyList(),
                    notFound = listOf(symbol),
                    errors = mapOf(symbol to "Symbol not found"),
                    requestTimeMs = requestTimeMs,
                    strategy = "single",
                    category = category
                )
            }
        } catch (e: Exception) {
            val requestTimeMs = System.currentTimeMillis() - startTime
            logger.error(e) { "Failed to fetch single price for $symbol" }
            return BatchPriceResult(
                successful = emptyList(),
                notFound = emptyList(),
                errors = mapOf(symbol to (e.message ?: "Unknown error")),
                requestTimeMs = requestTimeMs,
                strategy = "single",
                category = category
            )
        }
    }
    
    /**
     * Parallel processing for small batches
     */
    private suspend fun getParallelPrices(
        symbols: List<String>,
        category: MarketCategory,
        maxConcurrency: Int
    ): BatchPriceResult = coroutineScope {
        val semaphore = Semaphore(maxConcurrency)
        val startTime = System.currentTimeMillis()
        
        val deferredResults = symbols.map { symbol ->
            async {
                semaphore.withPermit {
                    try {
                        val response = bybitClient.getMarketTicker(symbol, category)
                        if (response.retCode == 0 && response.result.list.isNotEmpty()) {
                            PriceResult.Success(response.result.list.first())
                        } else {
                            PriceResult.NotFound(symbol)
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to fetch parallel price for $symbol" }
                        PriceResult.Error(symbol, e.message ?: "Unknown error")
                    }
                }
            }
        }
        
        val results = deferredResults.awaitAll()
        val requestTimeMs = System.currentTimeMillis() - startTime
        
        val successful = results.filterIsInstance<PriceResult.Success>().map { it.price }
        val notFound = results.filterIsInstance<PriceResult.NotFound>().map { it.symbol }
        val errors = results.filterIsInstance<PriceResult.Error>().associate { it.symbol to it.message }
        
        BatchPriceResult(
            successful = successful,
            notFound = notFound,
            errors = errors,
            requestTimeMs = requestTimeMs,
            strategy = "parallel",
            category = category
        )
    }
    
    /**
     * Batch API call for large symbol lists
     */
    private suspend fun getBatchApiPrices(
        symbols: List<String>,
        category: MarketCategory
    ): BatchPriceResult {
        val startTime = System.currentTimeMillis()
        try {
            // Get all symbols for the category and filter locally
            val response = bybitClient.getMarketTickers(emptyList(), category)
            val requestTimeMs = System.currentTimeMillis() - startTime
            
            if (response.retCode == 0) {
                val allTickers = response.result.list
                val requestedSymbolsSet = symbols.toSet()
                
                val successful = allTickers.filter { it.symbol in requestedSymbolsSet }
                val foundSymbols = successful.map { it.symbol }.toSet()
                val notFound = symbols.filter { it !in foundSymbols }
                
                logger.info { "Batch API: found ${successful.size}/${symbols.size} symbols" }
                
                return BatchPriceResult(
                    successful = successful,
                    notFound = notFound,
                    errors = emptyMap(),
                    requestTimeMs = requestTimeMs,
                    strategy = "batch",
                    category = category
                )
            } else {
                return BatchPriceResult(
                    successful = emptyList(),
                    notFound = emptyList(),
                    errors = mapOf("api" to "Batch API error: ${response.retMsg}"),
                    requestTimeMs = requestTimeMs,
                    strategy = "batch",
                    category = category
                )
            }
        } catch (e: Exception) {
            val requestTimeMs = System.currentTimeMillis() - startTime
            logger.error(e) { "Failed to fetch batch prices" }
            return BatchPriceResult(
                successful = emptyList(),
                notFound = emptyList(),
                errors = mapOf("exception" to (e.message ?: "Unknown error")),
                requestTimeMs = requestTimeMs,
                strategy = "batch",
                category = category
            )
        }
    }
}

/**
 * Result of batch price processing
 */
data class BatchPriceResult(
    val successful: List<BybitTickerItem>,
    val notFound: List<String>,
    val errors: Map<String, String>,
    val requestTimeMs: Long,
    val strategy: String,
    val category: MarketCategory
) {
    val totalRequested = successful.size + notFound.size + errors.size
    val successRate = if (totalRequested > 0) successful.size.toFloat() / totalRequested else 0f
}

/**
 * Individual price result for parallel processing
 */
sealed class PriceResult {
    data class Success(val price: BybitTickerItem) : PriceResult()
    data class NotFound(val symbol: String) : PriceResult()
    data class Error(val symbol: String, val message: String) : PriceResult()
}
