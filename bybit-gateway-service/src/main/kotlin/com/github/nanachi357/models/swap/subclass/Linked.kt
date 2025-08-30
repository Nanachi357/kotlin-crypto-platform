package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable

@Serializable
data class Linked(
    var deposits: MutableSet<String> = mutableSetOf(),
    var withdraws: MutableSet<String> = mutableSetOf(),
    var sellTrades: MutableSet<String> = mutableSetOf(),
    var buyTrades: MutableSet<String> = mutableSetOf(),
    var sellTransfers: MutableSet<String> = mutableSetOf(),
    var buyTransfers: MutableSet<String> = mutableSetOf(),
    var withdrawTransfers: MutableSet<String> = mutableSetOf(),
    var notes: MutableSet<String> = mutableSetOf(),
    var refunds: MutableSet<String> = mutableSetOf()
)
