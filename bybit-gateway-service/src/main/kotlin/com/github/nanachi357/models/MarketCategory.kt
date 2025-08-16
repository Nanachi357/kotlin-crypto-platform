package com.github.nanachi357.models

/**
 * Domain market categories that abstract from specific exchange implementations.
 * 
 * These categories represent the core business domain concepts and are independent
 * of how specific exchanges (Bybit, Binance, etc.) implement them.
 * 
 * @property exchangeValue The value used when making API calls to specific exchanges
 * @property description Human-readable description of the category
 */
enum class MarketCategory(
    val exchangeValue: String,
    val description: String
) {
    /**
     * Spot trading - immediate settlement of trades
     * Examples: BTCUSDT, ETHUSDT
     */
    SPOT("spot", "Spot trading with immediate settlement"),
    
    /**
     * Linear perpetual futures - USDT-margined contracts
     * Examples: BTCUSDT, ETHUSDT (as futures)
     */
    LINEAR("linear", "USDT-margined perpetual futures"),
    
    /**
     * Inverse perpetual futures - coin-margined contracts  
     * Examples: BTCUSD, ETHUSD
     */
    INVERSE("inverse", "Coin-margined perpetual futures"),
    
    /**
     * Options trading - derivative contracts with expiration
     * Examples: BTC-30JUN24-50000-C (Call), BTC-30JUN24-50000-P (Put)
     */
    OPTION("option", "Options trading with expiration dates");

    companion object {
        /**
         * Get category by exchange value (case-insensitive)
         */
        fun fromExchangeValue(value: String): MarketCategory? {
            return values().find { it.exchangeValue.equals(value, ignoreCase = true) }
        }
        
        /**
         * Get all categories that support ticker data
         */
        fun tickerSupportedCategories(): List<MarketCategory> = listOf(SPOT, LINEAR, INVERSE, OPTION)
        
        /**
         * Get categories that support real-time price data
         */
        fun realTimeSupportedCategories(): List<MarketCategory> = listOf(SPOT, LINEAR, INVERSE)
    }
}
