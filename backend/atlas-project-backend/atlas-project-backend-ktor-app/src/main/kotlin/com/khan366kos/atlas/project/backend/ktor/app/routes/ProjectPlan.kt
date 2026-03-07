package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Routing.projectPlan(
    repo: IAtlasProjectTaskRepo
) = route("/project-plan") {
    get {
        val plan = repo.projectPlan()
        call.respond(plan.toGanttDto())
    }
}