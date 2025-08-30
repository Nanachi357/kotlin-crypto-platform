package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var email: String?,
    var refundAddress: String?
)
