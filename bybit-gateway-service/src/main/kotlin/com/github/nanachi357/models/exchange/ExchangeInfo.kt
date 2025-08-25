package com.github.nanachi357.models.exchange

import kotlinx.serialization.Serializable

/**
 * Comprehensive exchange information including server time, rate limits, and symbols
 * 
 * @param exchange The exchange this information belongs to
 * @param serverTime Current server time in milliseconds
 * @param timezone Exchange server timezone
 * @param rateLimits List of rate limits for this exchange
 * @param symbols List of available trading symbols
 * @param status Exchange status (e.g., "TRADING", "MAINTENANCE")
 * @param permissions List of available permissions for the API key
 */
@Serializable
data class ExchangeInfo(
    val exchange: Exchange,
    val serverTime: Long,
    val timezone: String,
    val rateLimits: List<RateLimit>,
    val symbols: List<SymbolInfo>,
    val status: String = "TRADING",
    val permissions: List<String> = emptyList()
)

/**
 * Rate limit information for API endpoints
 * 
 * @param rateLimitType Type of rate limit (e.g., "REQUEST_WEIGHT", "ORDERS", "RAW_REQUESTS")
 * @param interval Time interval for the rate limit
 * @param intervalNum Number of intervals
 * @param limit Maximum number of requests allowed
 * @param currentUsage Current usage count (optional, may not be available)
 */
@Serializable
data class RateLimit(
    val rateLimitType: String,
    val interval: String,
    val intervalNum: Int,
    val limit: Int,
    val currentUsage: Int? = null
) {
    /**
     * Check if rate limit is exceeded
     */
    fun isExceeded(): Boolean = currentUsage?.let { it >= limit } ?: false
    
    /**
     * Get remaining requests
     */
    fun getRemaining(): Int? = currentUsage?.let { limit - it }
}

/**
 * Trading symbol information
 * 
 * @param symbol Trading pair symbol (e.g., "BTCUSDT")
 * @param status Symbol status (e.g., "TRADING", "BREAK", "AUCTION_MATCH")
 * @param baseAsset Base asset (e.g., "BTC")
 * @param quoteAsset Quote asset (e.g., "USDT")
 * @param minPrice Minimum price for orders
 * @param maxPrice Maximum price for orders
 * @param tickSize Minimum price change
 * @param minQty Minimum quantity for orders
 * @param maxQty Maximum quantity for orders
 * @param stepSize Minimum quantity change
 * @param minNotional Minimum notional value for orders
 * @param filters Additional filters for this symbol
 */
@Serializable
data class SymbolInfo(
    val symbol: String,
    val status: String,
    val baseAsset: String,
    val quoteAsset: String,
    val minPrice: String? = null,
    val maxPrice: String? = null,
    val tickSize: String? = null,
    val minQty: String? = null,
    val maxQty: String? = null,
    val stepSize: String? = null,
    val minNotional: String? = null,
    val filters: List<SymbolFilter> = emptyList()
) {
    /**
     * Check if symbol is currently trading
     */
    fun isTrading(): Boolean = status == "TRADING"
    
    /**
     * Get formatted symbol display name
     */
    fun getDisplayName(): String = "$baseAsset/$quoteAsset"
}

/**
 * Symbol filter for additional trading restrictions
 * 
 * @param filterType Type of filter (e.g., "PRICE_FILTER", "LOT_SIZE", "MIN_NOTIONAL")
 * @param minPrice Minimum price (for PRICE_FILTER)
 * @param maxPrice Maximum price (for PRICE_FILTER)
 * @param tickSize Minimum price change (for PRICE_FILTER)
 * @param minQty Minimum quantity (for LOT_SIZE)
 * @param maxQty Maximum quantity (for LOT_SIZE)
 * @param stepSize Minimum quantity change (for LOT_SIZE)
 * @param minNotional Minimum notional value (for MIN_NOTIONAL)
 */
@Serializable
data class SymbolFilter(
    val filterType: String,
    val minPrice: String? = null,
    val maxPrice: String? = null,
    val tickSize: String? = null,
    val minQty: String? = null,
    val maxQty: String? = null,
    val stepSize: String? = null,
    val minNotional: String? = null
)

/**
 * Exchange permissions for API key
 */
enum class ExchangePermission {
    SPOT_TRADING,
    MARGIN_TRADING,
    FUTURES_TRADING,
    OPTIONS_TRADING,
    LEVERAGED_TRADING,
    READ_ONLY,
    WITHDRAW,
    DEPOSIT
}

/**
 * Exchange status enum
 */
enum class ExchangeStatus {
    TRADING,
    MAINTENANCE,
    BREAK,
    AUCTION_MATCH,
    CLOSED
}
