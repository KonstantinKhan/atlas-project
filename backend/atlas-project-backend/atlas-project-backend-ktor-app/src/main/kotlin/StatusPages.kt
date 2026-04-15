package com.khan366kos

import com.khan366kos.atlas.project.backend.common.exceptions.PortfolioNotFoundException
import com.khan366kos.atlas.project.backend.common.exceptions.PortfolioOperationFailedException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest, mapOf(
                    "error" to "Invalid request format",
                    "details" to cause.message
                )
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest, mapOf(
                    "error" to "Validation failed",
                    "details" to cause.message
                )
            )
        }
        exception<PortfolioNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
        }
        exception<PortfolioOperationFailedException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error"))
        }
        exception<CancellationException> { call, cause ->
            // Log the cancellation for debugging purposes
            // TODO: Replace with proper logging framework (e.g., SLF4J)
            println("Request cancelled: ${cause.message}")
            call.respond(HttpStatusCode(499, "Client Closed Request"), mapOf("message" to cause.message))
        }
    }
}
