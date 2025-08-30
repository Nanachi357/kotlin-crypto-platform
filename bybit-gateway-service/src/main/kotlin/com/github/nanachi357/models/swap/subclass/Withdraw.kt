package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Withdraw(
    var coin: String,
    var network: String,
    var amount: String?,
    var address: String,
    var memo: String?,
    var platform: String
) {
    init {
        require(coin.isNotBlank()) { "coin cannot be blank" }
        require(network.isNotBlank()) { "network cannot be blank" }
        require(address.isNotBlank()) { "address cannot be blank" }
        require(platform.isNotBlank()) { "platform cannot be blank" }
        
        // Validate amount if present
        amount?.let { amountValue ->
            val amountBigDecimal = amountValue.toBigDecimalOrNull()
            require(amountBigDecimal != null && amountBigDecimal > BigDecimal.ZERO) { "amount must be a positive number" }
        }
    }

    fun getAmountAsBigDecimal(): BigDecimal? {
        return amount?.toBigDecimalOrNull()
    }
}
