package com.github.nanachi357.utils

import com.github.nanachi357.models.exchange.Exchange
import com.github.nanachi357.models.exchange.ExchangeResponse
import com.github.nanachi357.models.exchange.PriceData

/**
 * Enhanced error information for exchange-specific errors
 */
data class ExchangeErrorInfo(
    val message: String,
    val httpStatus: Int,
    val details: Map<String, String>,
    val exchange: String,
    val originalCode: String
)

/**
 * Utility functions for mapping between different response formats
 */
object ResponseMapper {
    
    /**
     * Create successful response
     */
    fun <T> success(
        data: T,
        exchange: Exchange,
        originalResponse: String? = null,
        includeDebugInfo: Boolean = false
    ): ExchangeResponse<T> {
        return ExchangeResponse(
            success = true,
            data = data,
            error = null,
            exchange = exchange,
            originalResponse = if (includeDebugInfo) originalResponse else null
        )
    }
    
    /**
     * Create error response
     */
    fun <T> error(
        error: String,
        exchange: Exchange,
        originalResponse: String? = null,
        includeDebugInfo: Boolean = false
    ): ExchangeResponse<T> {
        return ExchangeResponse(
            success = false,
            data = null,
            error = error,
            exchange = exchange,
            originalResponse = if (includeDebugInfo) originalResponse else null
        )
    }
    
    /**
     * Map Bybit ticker to universal price data
     */
    fun mapBybitTickerToPriceData(
        bybitTicker: com.github.nanachi357.models.bybit.BybitTickerItem
    ): PriceData {
        return PriceData(
            symbol = bybitTicker.symbol,
            price = bybitTicker.lastPrice,
            volume24h = bybitTicker.volume24h,
            change24h = bybitTicker.price24hPcnt,
            exchange = Exchange.BYBIT
        )
    }
    
    /**
     * Map Bybit response to universal response
     */
    fun <T> mapBybitResponse(
        bybitResponse: com.github.nanachi357.models.bybit.BybitResponse<T>,
        exchange: Exchange,
        mapper: (T) -> Any,
        includeDebugInfo: Boolean = false
    ): ExchangeResponse<Any> {
        return if (bybitResponse.retCode == 0) {
            success(
                data = mapper(bybitResponse.result),
                exchange = exchange,
                originalResponse = bybitResponse.toString(),
                includeDebugInfo = includeDebugInfo
            )
        } else {
            error(
                error = bybitResponse.retMsg,
                exchange = exchange,
                originalResponse = bybitResponse.toString(),
                includeDebugInfo = includeDebugInfo
            )
        }
    }
    
    /**
     * Map exchange-specific error to universal error format with enhanced details
     */
    fun mapExchangeError(
        errorCode: String,
        errorMessage: String,
        exchange: Exchange,
        includeDebugInfo: Boolean = false
    ): ExchangeErrorInfo {
        val (universalMessage, httpStatus, details) = when (exchange) {
            Exchange.BYBIT -> when (errorCode) {
                "10001" -> Triple("Invalid API key", 401, mapOf("field" to "apiKey", "constraint" to "valid"))
                "10002" -> Triple("Invalid signature", 401, mapOf("field" to "signature", "constraint" to "valid"))
                "10003" -> Triple("Invalid timestamp", 400, mapOf("field" to "timestamp", "constraint" to "current"))
                "10004" -> Triple("Invalid request", 400, mapOf("field" to "request", "constraint" to "valid"))
                "10005" -> Triple("Rate limit exceeded", 429, mapOf("field" to "rate", "constraint" to "limit"))
                else -> Triple(errorMessage, 502, mapOf("field" to "external", "constraint" to "unknown"))
            }
            Exchange.BINANCE -> when (errorCode) {
                "-2011" -> Triple("Invalid API key", 401, mapOf("field" to "apiKey", "constraint" to "valid"))
                "-2013" -> Triple("Invalid signature", 401, mapOf("field" to "signature", "constraint" to "valid"))
                "-2014" -> Triple("Invalid timestamp", 400, mapOf("field" to "timestamp", "constraint" to "current"))
                "-2015" -> Triple("Invalid request", 400, mapOf("field" to "request", "constraint" to "valid"))
                else -> Triple(errorMessage, 502, mapOf("field" to "external", "constraint" to "unknown"))
            }
            Exchange.COINBASE -> when (errorCode) {
                "authentication_error" -> Triple("Invalid API key", 401, mapOf("field" to "apiKey", "constraint" to "valid"))
                "invalid_signature" -> Triple("Invalid signature", 401, mapOf("field" to "signature", "constraint" to "valid"))
                "invalid_timestamp" -> Triple("Invalid timestamp", 400, mapOf("field" to "timestamp", "constraint" to "current"))
                "invalid_request" -> Triple("Invalid request", 400, mapOf("field" to "request", "constraint" to "valid"))
                else -> Triple(errorMessage, 502, mapOf("field" to "external", "constraint" to "unknown"))
            }
        }
        
        return ExchangeErrorInfo(
            message = universalMessage,
            httpStatus = httpStatus,
            details = details,
            exchange = exchange.name,
            originalCode = errorCode
        )
    }
    
    /**
     * Validate and sanitize symbol for security
     */
    fun validateSymbol(symbol: String): Boolean {
        return symbol.matches(Regex("^[A-Z0-9]+$")) && 
               symbol.length in 3..20 &&
               !symbol.contains("'") &&
               !symbol.contains("\"") &&
               !symbol.contains(";") &&
               !symbol.contains("--")
    }
}
