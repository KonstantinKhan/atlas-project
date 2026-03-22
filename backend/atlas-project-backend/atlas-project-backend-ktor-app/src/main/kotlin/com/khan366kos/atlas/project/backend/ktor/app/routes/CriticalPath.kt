package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.models.projectPlan.CriticalPathAnalysis
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.criticalPath(
    repo: IAtlasProjectTaskRepo,
    calendarService: CacheCalendarProvider,
) = route("/critical-path") {
    get {
        val projectId = call.parameters["projectId"]!!
        val plan = repo.projectPlan(projectId)
        val calendar = calendarService.current()
        val result = CriticalPathAnalysis(plan, calendar).compute()
        call.respond(result.toDto())
    }
}
