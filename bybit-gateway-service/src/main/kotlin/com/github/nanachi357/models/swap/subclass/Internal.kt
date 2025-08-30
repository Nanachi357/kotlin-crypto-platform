package com.github.nanachi357.models.swap.subclass

import com.github.nanachi357.models.swap.subclass.enums.SwapStep
import kotlinx.serialization.Serializable

@Serializable
data class Internal(
    var active: Boolean,
    var zipped: Boolean,
    var step: SwapStep,
    val virtual: Virtual
)
