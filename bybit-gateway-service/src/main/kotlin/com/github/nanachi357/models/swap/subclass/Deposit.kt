package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Deposit(
    var coin: String,
    var network: String,
    var amount: String,
    var actual: String?,
    var address: String,
    var memo: String?,
    var platform: String,
    var account: String
) {
    init {
        require(coin.isNotBlank()) { "coin cannot be blank" }
        require(network.isNotBlank()) { "network cannot be blank" }
        require(amount.isNotBlank()) { "amount cannot be blank" }
        require(address.isNotBlank()) { "address cannot be blank" }
        require(platform.isNotBlank()) { "platform cannot be blank" }
        require(account.isNotBlank()) { "account cannot be blank" }
        
        // Validate amount is positive
        val amountValue = amount.toBigDecimalOrNull()
        require(amountValue != null && amountValue > BigDecimal.ZERO) { "amount must be a positive number" }
        
        // Validate actual if present
        actual?.let { actualValue ->
            val actualBigDecimal = actualValue.toBigDecimalOrNull()
            require(actualBigDecimal != null && actualBigDecimal >= BigDecimal.ZERO) { "actual must be a non-negative number" }
        }
    }

    fun getAmountAsBigDecimal(): BigDecimal {
        return amount.toBigDecimal()
    }

    fun getActualAsBigDecimal(): BigDecimal? {
        return actual?.toBigDecimalOrNull()
    }
}
