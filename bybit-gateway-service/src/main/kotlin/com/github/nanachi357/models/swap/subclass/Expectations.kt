package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Expectations(
    var wait: String,
    var confirm: String?,
    var sellTransfer: String?,
    var sell: String?,
    var buyTransfer: String?,
    var buy: String?
) {
    init {
        require(wait.isNotBlank()) { "wait cannot be blank" }
        
        // Validate wait is positive
        val waitValue = wait.toBigDecimalOrNull()
        require(waitValue != null && waitValue > BigDecimal.ZERO) { "wait must be a positive number" }
        
        // Validate other fields if present
        confirm?.let { confirmValue ->
            val confirmBigDecimal = confirmValue.toBigDecimalOrNull()
            require(confirmBigDecimal != null && confirmBigDecimal >= BigDecimal.ZERO) { "confirm must be a non-negative number" }
        }
        
        sellTransfer?.let { sellTransferValue ->
            val sellTransferBigDecimal = sellTransferValue.toBigDecimalOrNull()
            require(sellTransferBigDecimal != null && sellTransferBigDecimal >= BigDecimal.ZERO) { "sellTransfer must be a non-negative number" }
        }
        
        sell?.let { sellValue ->
            val sellBigDecimal = sellValue.toBigDecimalOrNull()
            require(sellBigDecimal != null && sellBigDecimal >= BigDecimal.ZERO) { "sell must be a non-negative number" }
        }
        
        buyTransfer?.let { buyTransferValue ->
            val buyTransferBigDecimal = buyTransferValue.toBigDecimalOrNull()
            require(buyTransferBigDecimal != null && buyTransferBigDecimal >= BigDecimal.ZERO) { "buyTransfer must be a non-negative number" }
        }
        
        buy?.let { buyValue ->
            val buyBigDecimal = buyValue.toBigDecimalOrNull()
            require(buyBigDecimal != null && buyBigDecimal >= BigDecimal.ZERO) { "buy must be a non-negative number" }
        }
    }

    fun getWaitAsBigDecimal(): BigDecimal {
        return wait.toBigDecimal()
    }

    fun getConfirmAsBigDecimal(): BigDecimal? {
        return confirm?.toBigDecimalOrNull()
    }

    fun getSellTransferAsBigDecimal(): BigDecimal? {
        return sellTransfer?.toBigDecimalOrNull()
    }

    fun getSellAsBigDecimal(): BigDecimal? {
        return sell?.toBigDecimalOrNull()
    }

    fun getBuyTransferAsBigDecimal(): BigDecimal? {
        return buyTransfer?.toBigDecimalOrNull()
    }

    fun getBuyAsBigDecimal(): BigDecimal? {
        return buy?.toBigDecimalOrNull()
    }
}
