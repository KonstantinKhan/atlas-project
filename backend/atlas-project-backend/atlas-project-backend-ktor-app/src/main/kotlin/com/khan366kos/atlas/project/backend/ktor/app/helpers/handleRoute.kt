package com.khan366kos.atlas.project.backend.ktor.app.helpers

import com.khan366kos.atlas.project.backend.ktor.app.helpers.handleParameters
import io.ktor.server.application.ApplicationCall

suspend inline fun ApplicationCall.handleRoute(
    crossinline block: suspend () -> Unit
) {
    handleParameters {
        block()
    }
}