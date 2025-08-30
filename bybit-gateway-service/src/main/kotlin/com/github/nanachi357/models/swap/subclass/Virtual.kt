package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable

@Serializable
data class Virtual(
    var deposit: Boolean,
    var withdraw: Boolean
)
