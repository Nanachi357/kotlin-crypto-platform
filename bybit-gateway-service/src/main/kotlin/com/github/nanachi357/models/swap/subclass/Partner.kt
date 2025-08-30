package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Partner(
    var key: String,
    var fee: String,
    var profit: Profit,
    var name: String
) {
    init {
        require(key.isNotBlank()) { "key cannot be blank" }
        require(fee.isNotBlank()) { "fee cannot be blank" }
        require(name.isNotBlank()) { "name cannot be blank" }
        
        // Validate fee is a valid number
        val feeValue = fee.toBigDecimalOrNull()
        require(feeValue != null && feeValue >= BigDecimal.ZERO) { "fee must be a non-negative number" }
    }

    fun getFeeAsBigDecimal(): BigDecimal {
        return fee.toBigDecimal()
    }
}
