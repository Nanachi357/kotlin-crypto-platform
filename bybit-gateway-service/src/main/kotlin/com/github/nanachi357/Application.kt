package com.github.nanachi357

import com.github.nanachi357.clients.BybitApiClient
import com.github.nanachi357.plugins.configureRouting
import com.github.nanachi357.plugins.configureErrorHandling
import com.github.nanachi357.services.PriceService
import com.github.nanachi357.utils.HttpClientFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.http.*

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
    
    // Configure CORS for web clients
    install(io.ktor.server.plugins.cors.routing.CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
    }
    
    // Create HTTP client for external API calls
    val httpClient = HttpClientFactory.create()
    val bybitClient = BybitApiClient(httpClient)
    
    // Create service layer with functional error handling
    val priceService = PriceService(bybitClient)
    
    // Configure error handling (must be before routing)
    configureErrorHandling()
    
    // Configure routing with service layer
    configureRouting(priceService)
}