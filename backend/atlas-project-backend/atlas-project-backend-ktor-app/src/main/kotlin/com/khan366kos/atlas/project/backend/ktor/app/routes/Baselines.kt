package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class SaveBaselineRequest(
    val taskIds: List<String>? = null,
)

fun Route.baselines(
    repo: IAtlasProjectTaskRepo,
) = route("/save-baseline") {
    post {
        val projectId = call.parameters["projectId"]!!
        repo.saveBaseline(projectId)
        call.respond(HttpStatusCode.OK)
    }
}
