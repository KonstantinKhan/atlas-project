package com.khan366kos.atlas.project.backend.ktor.app.plugins

import com.khan366kos.atlas.project.backend.ktor.app.routes.projectPlan
import com.khan366kos.config.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(
    appConfig: AppConfig
) {
    routing {
        projectPlan(appConfig.repo)
    }
}