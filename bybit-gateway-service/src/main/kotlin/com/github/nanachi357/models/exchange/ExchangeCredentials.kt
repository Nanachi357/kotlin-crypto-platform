package com.github.nanachi357.models.exchange

import kotlinx.serialization.Serializable

/**
 * Universal credentials for all exchanges
 */
@Serializable
data class ExchangeCredentials(
    val apiKey: String,
    val secretKey: String,
    val exchange: Exchange,
    val testnet: Boolean = true,
    val additionalParams: Map<String, String> = emptyMap()
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(secretKey.isNotBlank()) { "Secret key cannot be blank" }
    }

    fun getApiUrl(): String = exchange.getApiUrl(testnet)

    fun getFullApiUrl(endpoint: String): String = exchange.getFullApiUrl(endpoint, testnet)
}

/**
 * Universal authentication headers
 */
data class ExchangeAuthHeaders(
    val apiKey: String,
    val timestamp: Long,
    val signature: String,
    val exchange: Exchange,
    val additionalHeaders: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, String> {
        val baseHeaders = when (exchange) {
            Exchange.BYBIT -> mapOf(
                "X-BAPI-API-KEY" to apiKey,
                "X-BAPI-TIMESTAMP" to timestamp.toString(),
                "X-BAPI-SIGN" to signature,
                "X-BAPI-RECV-WINDOW" to "5000"
            )
            Exchange.BINANCE -> mapOf(
                "X-MBX-APIKEY" to apiKey
            )
            Exchange.COINBASE -> mapOf(
                "CB-ACCESS-KEY" to apiKey,
                "CB-ACCESS-TIMESTAMP" to timestamp.toString(),
                "CB-ACCESS-SIGN" to signature
            )
        }
        
        return baseHeaders + additionalHeaders
    }
}
