package com.github.nanachi357.models.exchange

import kotlinx.serialization.Serializable

/**
 * Universal response wrapper for all exchanges
 */
@Serializable
data class ExchangeResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String?,
    val exchange: Exchange,
    val timestamp: Long = System.currentTimeMillis(),
    val originalResponse: String? = null // For debugging
)

/**
 * Universal price data model
 */
@Serializable
data class PriceData(
    val symbol: String,
    val price: String,
    val volume24h: String? = null,
    val change24h: String? = null,
    val high24h: String? = null,
    val low24h: String? = null,
    val exchange: Exchange
)

/**
 * Universal batch price response
 */
@Serializable
data class BatchPriceResponse(
    val prices: List<PriceData>,
    val exchange: Exchange,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Universal order data model
 */
@Serializable
data class OrderData(
    val orderId: String,
    val symbol: String,
    val side: OrderSide,
    val type: OrderType,
    val quantity: String,
    val price: String?,
    val status: OrderStatus,
    val exchange: Exchange,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Order side enum
 */
enum class OrderSide {
    BUY, SELL
}

/**
 * Order type enum
 */
enum class OrderType {
    MARKET, LIMIT, STOP_LOSS, TAKE_PROFIT
}

/**
 * Order status enum
 */
enum class OrderStatus {
    PENDING, FILLED, PARTIALLY_FILLED, CANCELLED, REJECTED
}

/**
 * Universal balance data model
 */
@Serializable
data class BalanceData(
    val asset: String,
    val free: String,
    val locked: String,
    val total: String,
    val exchange: Exchange
)
