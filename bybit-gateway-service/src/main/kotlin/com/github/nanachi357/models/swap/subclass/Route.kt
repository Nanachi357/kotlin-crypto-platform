package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    var sell: RouteData,
    var buy: RouteData,
    var withdraw: RouteData
)
