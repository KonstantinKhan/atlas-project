package com.khan366kos

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(/StatusPages) {
        exception<SerializationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Invalid request format",
                "details" to cause.message
            ))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Validation failed",
                "details" to cause.message
            ))
        }
    }
}
