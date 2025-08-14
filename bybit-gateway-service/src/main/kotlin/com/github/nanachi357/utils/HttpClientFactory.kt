package com.github.nanachi357.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.Duration
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel

/**
 * Factory for creating configured HTTP clients for external API calls.
 * 
 * Provides consistent configuration for all external API integrations
 * with proper JSON serialization and logging.
 */
object HttpClientFactory {
    
    /**
     * Creates a configured HTTP client for external API calls.
     * 
     * @return Configured HttpClient with OkHttp engine and JSON support
     */
    fun create(): HttpClient {
        return HttpClient(OkHttp) {
            // JSON handling for external APIs
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true    // Bybit sends extra fields
                    isLenient = true           // Accept slightly malformed JSON
                    prettyPrint = false        // Compact for network efficiency
                })
            }
            
            // Request/response logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            // Configure timeouts
            engine {
                config {
                    connectTimeout(Duration.ofSeconds(10))  // 10 seconds
                    readTimeout(Duration.ofSeconds(20))     // 20 seconds
                }
            }
        }
    }
}
