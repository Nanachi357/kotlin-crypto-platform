package com.github.nanachi357.models.bybit

import kotlinx.serialization.Serializable

/**
 * Standard Bybit API response wrapper.
 * 
 * All Bybit API responses follow this structure with retCode, retMsg, result, and time.
 * 
 * @param T The type of data in the result field
 */
@Serializable
data class BybitResponse<T>(
    val retCode: Int,
    val retMsg: String,
    val result: T,
    val retExtInfo: Map<String, String>? = null,
    val time: Long
)

/**
 * Bybit server time response.
 * 
 * Used for testing API connectivity and getting server time.
 */
@Serializable
data class BybitTimeResult(
    val timeSecond: String,
    val timeNano: String
)

/**
 * Market ticker data from Bybit API.
 * 
 * Contains current market information for a trading pair.
 */
@Serializable
data class BybitTickerResult(
    val category: String,
    val list: List<BybitTickerItem>
)

/**
 * Individual ticker item with market data.
 */
@Serializable
data class BybitTickerItem(
    val symbol: String,
    val lastPrice: String,
    val bid1Price: String,
    val ask1Price: String,
    val volume24h: String,
    val turnover24h: String,
    val price24hPcnt: String,
    val usdIndexPrice: String? = null
)
