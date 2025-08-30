package com.github.nanachi357.models.swap.subclass

import kotlinx.serialization.Serializable

@Serializable
data class RouteData(
    var platform: String,
    var address: String,
    var memo: String?,
    var account: String
) {
    init {
        require(platform.isNotBlank()) { "platform cannot be blank" }
        require(address.isNotBlank()) { "address cannot be blank" }
        require(account.isNotBlank()) { "account cannot be blank" }
    }
}
