package com.khan366kos.atlas.project.backend.ktor.app.helpers

import io.ktor.server.application.ApplicationCall

suspend inline fun ApplicationCall.handleRoute(
    crossinline block: suspend () -> Unit
) {
    handleProjectId {
        block()
    }
}