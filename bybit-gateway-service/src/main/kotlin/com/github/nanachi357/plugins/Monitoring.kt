package com.github.nanachi357.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import mu.KotlinLogging
import org.slf4j.event.Level
import org.slf4j.MDC

private val logger = KotlinLogging.logger {}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        
        // Filter out health checks and static endpoints
        filter { call ->
            val path = call.request.path()
            !path.startsWith("/health") && path != "/"
        }
        

        
        // Custom format with performance metrics
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val userAgent = call.request.headers["User-Agent"]
            val requestId = call.request.headers["X-Request-ID"] ?: call.request.hashCode().toString()
            
            // Set MDC for request tracking
            MDC.put("requestId", requestId)
            
            // Structured log format
            "HTTP_REQUEST: method=$httpMethod, uri=$uri, status=$status, userAgent=$userAgent"
        }
    }
    
    logger.info { "Monitoring plugin configured successfully" }
}
