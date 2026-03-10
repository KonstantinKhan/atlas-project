package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class ReorderRequest(val orderedIds: List<String>)

fun Routing.reorderTasks(
    repo: IAtlasProjectTaskRepo
) = route("/reorder-tasks") {
    post {
        val request = call.receive<ReorderRequest>()
        repo.reorderTasks(request.orderedIds)
        val plan = repo.projectPlan()
        call.respond(plan.toGanttDto())
    }
}
