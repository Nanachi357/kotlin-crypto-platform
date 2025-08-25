package com.github.nanachi357.models.exchange

/**
 * Supported cryptocurrency exchanges
 */
enum class Exchange(
    val displayName: String,
    val baseUrl: String,
    val testnetUrl: String,
    val apiVersion: String,
    val requiresAuth: Boolean = true
) {
    BYBIT(
        displayName = "Bybit",
        baseUrl = "https://api.bybit.com",
        testnetUrl = "https://api-testnet.bybit.com",
        apiVersion = "v5"
    ),
    BINANCE(
        displayName = "Binance",
        baseUrl = "https://api.binance.com",
        testnetUrl = "https://testnet.binance.vision",
        apiVersion = "v3"
    ),
    COINBASE(
        displayName = "Coinbase",
        baseUrl = "https://api.coinbase.com",
        testnetUrl = "https://api-public.sandbox.exchange.coinbase.com",
        apiVersion = "v2"
    );

    fun getApiUrl(testnet: Boolean = false): String {
        return if (testnet) testnetUrl else baseUrl
    }

    fun getFullApiUrl(endpoint: String, testnet: Boolean = false): String {
        return "${getApiUrl(testnet)}/$apiVersion/$endpoint"
    }
}
