package com.github.nanachi357.models

import kotlinx.serialization.Serializable

/**
 * API credentials for Bybit authentication
 */
@Serializable
data class BybitCredentials(
    val apiKey: String,
    val secretKey: String,
    val testnet: Boolean = true
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(secretKey.isNotBlank()) { "Secret key cannot be blank" }
    }
}

/**
 * Authentication headers for Bybit API requests
 */
data class BybitAuthHeaders(
    val apiKey: String,
    val timestamp: Long,
    val signature: String,
    val recvWindow: Long = 5000
) {
    fun toMap(): Map<String, String> = mapOf(
        "X-BAPI-API-KEY" to apiKey,
        "X-BAPI-TIMESTAMP" to timestamp.toString(),
        "X-BAPI-SIGN" to signature,
        "X-BAPI-RECV-WINDOW" to recvWindow.toString()
    )
}

/**
 * Authentication request context
 */
data class AuthRequestContext(
    val credentials: BybitCredentials,
    val timestamp: Long = System.currentTimeMillis(),
    val recvWindow: Long = 5000
) {
    fun getBaseUrl(): String = if (credentials.testnet) {
        "https://api-testnet.bybit.com"
    } else {
        "https://api.bybit.com"
    }
}

/**
 * Authentication error responses
 */
@Serializable
data class AuthErrorResponse(
    val retCode: Int,
    val retMsg: String,
    val retExtInfo: Map<String, String>? = null,
    val time: Long = System.currentTimeMillis()
)

/**
 * Common authentication error codes
 */
object AuthErrorCodes {
    const val INVALID_API_KEY = 10001
    const val INVALID_SIGNATURE = 10002
    const val INVALID_TIMESTAMP = 10003
    const val INVALID_RECV_WINDOW = 10004
    const val RATE_LIMIT_EXCEEDED = 10005
    const val INSUFFICIENT_PERMISSIONS = 10006
}
