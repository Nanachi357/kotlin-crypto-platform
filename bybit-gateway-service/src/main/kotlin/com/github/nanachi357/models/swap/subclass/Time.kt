package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable

@Serializable
data class Time(
    var create: Long,
    var confirm: Long?,
    var sellTransfer: Long?,
    var sell: Long?,
    var buyTransfer: Long?,
    var buy: Long?,
    var withdrawTransfer: Long?,
    var send: Long?,
    var success: Long?,
    var overdue: Long?,
    var refund: Long?,
    var frozen: Long?,
    var suspended: Long?,
    var cancel: Long?
) {
    init {
        require(create > 0) { "create timestamp must be positive" }
        
        // Validate all timestamps are positive if present
        confirm?.let { require(it > 0) { "confirm timestamp must be positive" } }
        sellTransfer?.let { require(it > 0) { "sellTransfer timestamp must be positive" } }
        sell?.let { require(it > 0) { "sell timestamp must be positive" } }
        buyTransfer?.let { require(it > 0) { "buyTransfer timestamp must be positive" } }
        buy?.let { require(it > 0) { "buy timestamp must be positive" } }
        withdrawTransfer?.let { require(it > 0) { "withdrawTransfer timestamp must be positive" } }
        send?.let { require(it > 0) { "send timestamp must be positive" } }
        success?.let { require(it > 0) { "success timestamp must be positive" } }
        overdue?.let { require(it > 0) { "overdue timestamp must be positive" } }
        refund?.let { require(it > 0) { "refund timestamp must be positive" } }
        frozen?.let { require(it > 0) { "frozen timestamp must be positive" } }
        suspended?.let { require(it > 0) { "suspended timestamp must be positive" } }
        cancel?.let { require(it > 0) { "cancel timestamp must be positive" } }
    }
}
