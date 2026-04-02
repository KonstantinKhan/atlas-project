package com.khan366kos.atlas.project.backend.ktor.app.plugins

import com.khan366kos.atlas.project.backend.ktor.app.routes.analysis
import com.khan366kos.atlas.project.backend.ktor.app.routes.assignments
import com.khan366kos.atlas.project.backend.ktor.app.routes.baselines
import com.khan366kos.atlas.project.backend.ktor.app.routes.criticalPath
import com.khan366kos.atlas.project.backend.ktor.app.routes.leveling
import com.khan366kos.atlas.project.backend.ktor.app.routes.portfolios
import com.khan366kos.atlas.project.backend.ktor.app.routes.projectPlan
import com.khan366kos.atlas.project.backend.ktor.app.routes.reorderTasks
import com.khan366kos.atlas.project.backend.ktor.app.routes.resourceLoad
import com.khan366kos.atlas.project.backend.ktor.app.routes.resources
import com.khan366kos.atlas.project.backend.project.service.ProjectService
import com.khan366kos.config.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    appConfig: AppConfig,
    projectService: ProjectService
) {
    routing {
        portfolios(
            appConfig.portfolioRepo,
            appConfig.repo,
            appConfig.resourceRepo,
            appConfig.calendarService,
            appConfig.portfolioService
        )
        resourceLoad(appConfig.portfolioRepo, appConfig.repo, appConfig.resourceRepo, appConfig.calendarService)
        resources(appConfig.resourceRepo)

        route("/projects/{projectId}") {
            projectPlan(appConfig.repo, appConfig.resourceRepo, appConfig.calendarService)
            criticalPath(appConfig.repo, appConfig.calendarService)
            analysis(appConfig.repo, appConfig.calendarService)
            reorderTasks(appConfig.repo, appConfig.resourceRepo, appConfig.calendarService)
            assignments(appConfig.repo, appConfig.resourceRepo, appConfig.calendarService)
            leveling(appConfig.repo, appConfig.resourceRepo, appConfig.calendarService)
            baselines(appConfig.repo)
        }
    }
}
