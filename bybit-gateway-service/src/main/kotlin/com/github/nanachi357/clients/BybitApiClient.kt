package com.github.nanachi357.clients

import com.github.nanachi357.models.bybit.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * HTTP client for interacting with Bybit V5 API.
 * 
 * Provides methods for making authenticated and public API calls
 * to Bybit exchange with proper error handling.
 */
class BybitApiClient(val httpClient: HttpClient) {
    
    companion object {
        // Base URLs for different environments
        private const val MAINNET_URL = "https://api.bybit.com"
        private const val TESTNET_URL = "https://api-testnet.bybit.com"
        private const val DEMO_URL = "https://api-demo.bybit.com"
        
        /**
         * Gets the appropriate base URL based on environment.
         * 
         * @return Base URL for Bybit API
         */
        fun getBaseUrl(): String {
            return when (System.getenv("BYBIT_ENV")?.uppercase()) {
                "MAINNET" -> MAINNET_URL
                "DEMO" -> DEMO_URL
                else -> TESTNET_URL  // Default to testnet for development
            }
        }
    }
    
    val baseUrl = getBaseUrl()
    
    /**
     * Gets Bybit server time for testing API connectivity.
     * 
     * @return Server time response from Bybit
     */
    suspend fun getServerTime(): BybitResponse<BybitTimeResult> {
        return httpClient.get("$baseUrl/v5/market/time").body()
    }
    
    /**
     * Gets market ticker data for a specific symbol.
     * 
     * @param symbol Trading pair symbol (e.g., "BTCUSDT")
     * @param category Market category (default: "spot")
     * @return Market ticker data
     */
    suspend fun getMarketTicker(
        symbol: String,
        category: String = "spot"
    ): BybitResponse<BybitTickerResult> {
        return httpClient.get("$baseUrl/v5/market/tickers") {
            parameter("category", category)
            parameter("symbol", symbol)
        }.body()
    }
    
    /**
     * Gets market ticker data for multiple symbols.
     * 
     * Note: Bybit API supports getting all symbols when symbol parameter is not provided.
     * For specific symbols, we make separate requests for each symbol and combine results.
     * 
     * @param symbols List of trading pair symbols (optional - if empty, returns all symbols)
     * @param category Market category (default: "spot")
     * @return Market ticker data for specified symbols or all symbols
     */
    suspend fun getMarketTickers(
        symbols: List<String> = emptyList(),
        category: String = "spot"
    ): BybitResponse<BybitTickerResult> {
        // If no symbols specified, get all symbols
        if (symbols.isEmpty()) {
            return httpClient.get("$baseUrl/v5/market/tickers") {
                parameter("category", category)
            }.body()
        }
        
        // For multiple symbols, make separate requests and combine results
        val allTickers = mutableListOf<BybitTickerItem>()
        
        for (symbol in symbols) {
            try {
                val response = getMarketTicker(symbol, category)
                if (response.retCode == 0 && response.result.list.isNotEmpty()) {
                    allTickers.addAll(response.result.list)
                }
            } catch (e: Exception) {
                // Log error but continue with other symbols
                // TODO: Replace with proper logging
            }
        }
        
        // Return combined result
        return BybitResponse(
            retCode = 0,
            retMsg = "OK",
            result = BybitTickerResult(
                category = category,
                list = allTickers
            ),
            time = System.currentTimeMillis()
        )
    }
    
    /**
     * Closes the HTTP client and releases resources.
     */
    fun close() {
        httpClient.close()
    }
}
