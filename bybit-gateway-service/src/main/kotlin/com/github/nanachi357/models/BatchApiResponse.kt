package com.github.nanachi357.models

import com.github.nanachi357.models.bybit.BybitTickerItem
import kotlinx.serialization.Serializable

/**
 * Response for batch market API endpoint
 */
@Serializable
data class BatchApiResponse(
    val prices: @Serializable List<PriceInfo>,
    val metadata: BatchMetadata,
    val notFound: @Serializable List<String>,
    val errors: @Serializable Map<String, String>
)

/**
 * Price information for a symbol
 */
@Serializable
data class PriceInfo(
    val symbol: String,
    val lastPrice: String,
    val price24hPcnt: String,
    val volume24h: String
)

/**
 * Metadata about the batch request
 */
@Serializable
data class BatchMetadata(
    val strategy: String,
    val category: String,
    val requestTimeMs: Long,
    val successCount: Int,
    val notFoundCount: Int,
    val errorCount: Int,
    val successRate: Float
)

/**
 * Extension function to convert BybitTickerItem to PriceInfo
 */
fun BybitTickerItem.toPriceInfo(): PriceInfo = PriceInfo(
    symbol = symbol,
    lastPrice = lastPrice,
    price24hPcnt = price24hPcnt,
    volume24h = volume24h
)
