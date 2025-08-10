package com.github.nanachi357.models

import kotlinx.serialization.Serializable

/**
 * Server status information for health check and monitoring.
 * 
 * @param status Current server status (e.g., "OK", "ERROR", "MAINTENANCE")
 * @param timestamp ISO 8601 formatted timestamp of the status check
 * @param version Semantic version of the API
 * @param uptime Server uptime in milliseconds since startup
 */
@Serializable
data class ServerStatus(
    val status: String,
    val timestamp: String,
    val version: String,
    val uptime: Long
)
