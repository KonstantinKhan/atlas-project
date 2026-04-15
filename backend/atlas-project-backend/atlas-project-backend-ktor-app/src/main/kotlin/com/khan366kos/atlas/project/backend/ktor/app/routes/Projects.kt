package com.khan366kos.com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.mappers.toResponseProjectDto
import com.khan366kos.atlas.project.backend.project.service.ProjectService
import com.khan366kos.atlas.project.backend.transport.responses.project.SearchProjectResponseDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Routing.projects(
    projectService: ProjectService,
) = route("/portfolios/{id}/projects") {
    get {
        val portfolioId = call.parameters["id"]!!
        val projects = projectService.portfolioProjects(PortfolioId(portfolioId))
        call.respond(
            SearchProjectResponseDto(
                projects = projects.map { it.toResponseProjectDto() },
            )
        )
    }
}