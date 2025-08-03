package com.github.nanachi357.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/health") {
            call.respondText(
                text = "OK", 
                status = HttpStatusCode.OK
            )
        }
        
        // Service information endpoint
        get("/") {
            call.respondText(
                text = "Bybit Gateway API - Phase 1",
                status = HttpStatusCode.OK
            )
        }
    }
}