package com.github.nanachi357.clients

import com.github.nanachi357.models.exchange.*

/**
 * Universal interface for all exchange API clients
 */
interface ExchangeApiClient {
    
    /**
     * Get current price for a symbol
     */
    suspend fun getPrice(symbol: String): ExchangeResponse<PriceData>
    
    /**
     * Get prices for multiple symbols
     */
    suspend fun getPrices(symbols: List<String>): ExchangeResponse<BatchPriceResponse>
    
    /**
     * Get account balance
     */
    suspend fun getBalance(): ExchangeResponse<List<BalanceData>>
    
    /**
     * Create a new order
     */
    suspend fun createOrder(
        symbol: String,
        side: OrderSide,
        type: OrderType,
        quantity: String,
        price: String? = null
    ): ExchangeResponse<OrderData>
    
    /**
     * Get order status
     */
    suspend fun getOrderStatus(orderId: String): ExchangeResponse<OrderData>
    
    /**
     * Cancel an order
     */
    suspend fun cancelOrder(orderId: String): ExchangeResponse<Boolean>
    
    /**
     * Get exchange information including symbols, rate limits, and server time
     */
    suspend fun getExchangeInfo(): ExchangeResponse<ExchangeInfo>
    
    /**
     * Get symbol information for a specific trading pair
     */
    suspend fun getSymbolInfo(symbol: String): ExchangeResponse<SymbolInfo>
    
    /**
     * Get all available symbols for this exchange
     */
    suspend fun getAllSymbols(): ExchangeResponse<List<SymbolInfo>>
    
    /**
     * Get rate limits for this exchange
     */
    suspend fun getRateLimits(): ExchangeResponse<List<RateLimit>>
    
    /**
     * Test connectivity to the exchange
     */
    suspend fun testConnectivity(): ExchangeResponse<Boolean>
    
    /**
     * Get server time from the exchange
     */
    suspend fun getServerTime(): ExchangeResponse<Long>
}
