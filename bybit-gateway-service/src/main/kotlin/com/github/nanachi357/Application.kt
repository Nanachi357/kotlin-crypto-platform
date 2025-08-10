package com.github.nanachi357

import com.github.nanachi357.clients.BybitApiClient
import com.github.nanachi357.plugins.configureRouting
import com.github.nanachi357.utils.HttpClientFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty, 
        port = 8080, 
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Configure JSON serialization for server responses
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    
    // Create HTTP client for external API calls
    val httpClient = HttpClientFactory.create()
    val bybitClient = BybitApiClient(httpClient)
    
    // Configure routing with HTTP client
    configureRouting(bybitClient)
}