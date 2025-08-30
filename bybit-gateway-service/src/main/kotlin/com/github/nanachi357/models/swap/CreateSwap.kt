package com.github.nanachi357.models.swap

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateSwap(
    val assetGive: String,
    val assetTake: String,
    val networkGive: String,
    val networkTake: String,
    val addressTake: String,
    val amountGive: String,
    val type: String,
    var memoTake: String? = null,
    var email: String? = null,
    var refundAddress: String? = null
) {
    init {
        require(assetGive.isNotBlank()) { "assetGive cannot be blank" }
        require(assetTake.isNotBlank()) { "assetTake cannot be blank" }
        require(networkGive.isNotBlank()) { "networkGive cannot be blank" }
        require(networkTake.isNotBlank()) { "networkTake cannot be blank" }
        require(addressTake.isNotBlank()) { "addressTake cannot be blank" }
        require(amountGive.isNotBlank()) { "amountGive cannot be blank" }
        require(type.isNotBlank()) { "type cannot be blank" }
        
        // Validate amount is positive
        val amount = amountGive.toBigDecimalOrNull()
        require(amount != null && amount > BigDecimal.ZERO) { "amountGive must be a positive number" }
    }

    fun getAmountGiveAsBigDecimal(): BigDecimal {
        return amountGive.toBigDecimal()
    }
}
