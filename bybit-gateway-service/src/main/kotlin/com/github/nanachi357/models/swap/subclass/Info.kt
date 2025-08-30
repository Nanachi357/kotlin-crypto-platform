package com.github.nanachi357.models.swap.subclass

import com.github.nanachi357.models.swap.subclass.enums.SwapStatus
import com.github.nanachi357.models.swap.subclass.enums.SwapType
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Info(
    var uid: String,
    var status: SwapStatus,
    var type: SwapType,
    var fee: String,
    var link: String,
    var equivalent: String
) {
    init {
        require(uid.isNotBlank()) { "uid cannot be blank" }
        require(link.isNotBlank()) { "link cannot be blank" }
        require(fee.isNotBlank()) { "fee cannot be blank" }
        require(equivalent.isNotBlank()) { "equivalent cannot be blank" }
        
        // Validate fee and equivalent are valid numbers
        require(fee.toBigDecimalOrNull() != null) { "fee must be a valid number" }
        require(equivalent.toBigDecimalOrNull() != null) { "equivalent must be a valid number" }
    }

    fun getFeeAsBigDecimal(): BigDecimal {
        return fee.toBigDecimal()
    }

    fun getEquivalentAsBigDecimal(): BigDecimal {
        return equivalent.toBigDecimal()
    }
}
