package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.calendar.service.CacheCalendarProvider
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class ReorderRequest(val orderedIds: List<String>)

fun Route.reorderTasks(
    repo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) = route("/reorder-tasks") {
    post {
        val projectId = call.parameters["projectId"]!!
        val request = call.receive<ReorderRequest>()
        repo.reorderTasks(request.orderedIds)
        val plan = repo.projectPlan(projectId)
        val assignments = resourceRepo.listAssignments(plan.id.asString())
        val calendar = calendarService.current()
        call.respond(plan.toGanttDto(assignments, calendar))
    }
}
