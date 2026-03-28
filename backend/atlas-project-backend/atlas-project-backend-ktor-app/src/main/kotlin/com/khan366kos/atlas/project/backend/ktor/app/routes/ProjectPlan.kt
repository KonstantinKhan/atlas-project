package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.ktor.app.helpers.handleProjectId
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.projectPlan(
    repo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) = route("/plan") {
    get {
        call.handleProjectId {
            val plan = repo.projectPlan(it.asString())
            val assignments = resourceRepo.listAssignments(plan.id.asString())
            val calendar = calendarService.current()
            call.respond(plan.toGanttDto(assignments, calendar))
        }
    }
}
