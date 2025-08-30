package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Profit(
    var amount: String,
    var percent: String
) {
    init {
        require(amount.isNotBlank()) { "amount cannot be blank" }
        require(percent.isNotBlank()) { "percent cannot be blank" }
        
        // Validate amount and percent are valid numbers
        val amountValue = amount.toBigDecimalOrNull()
        require(amountValue != null && amountValue >= BigDecimal.ZERO) { "amount must be a non-negative number" }
        
        val percentValue = percent.toBigDecimalOrNull()
        require(percentValue != null && percentValue >= BigDecimal.ZERO) { "percent must be a non-negative number" }
    }

    fun getAmountAsBigDecimal(): BigDecimal {
        return amount.toBigDecimal()
    }

    fun getPercentAsBigDecimal(): BigDecimal {
        return percent.toBigDecimal()
    }
}
