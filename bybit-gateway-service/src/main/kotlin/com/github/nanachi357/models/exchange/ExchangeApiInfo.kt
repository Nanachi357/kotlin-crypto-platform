package com.github.nanachi357.models.exchange

import kotlinx.serialization.Serializable

/**
 * Information about Universal Exchange API endpoints
 */
@Serializable
data class ExchangeApiInfo(
    val message: String,
    val endpoints: Map<String, String>,
    val examples: List<String>,
    val format: String
)
