package com.github.nanachi357.models.swap

import com.github.nanachi357.models.swap.subclass.*
import kotlinx.serialization.Serializable

/**
 * Universal abstraction for swap operations
 */
@Serializable
data class SwapOperation(
    var info: Info,
    var deposit: Deposit,
    var withdraw: Withdraw,
    var expectations: Expectations,
    var time: Time,
    var profit: Profit,
    var partner: Partner,
    var internal: Internal,
    var user: User,
    var linked: Linked,
    var route: Route
)
