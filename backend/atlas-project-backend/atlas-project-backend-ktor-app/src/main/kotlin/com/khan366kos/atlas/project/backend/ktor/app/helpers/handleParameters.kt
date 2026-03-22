package com.khan366kos.atlas.project.backend.ktor.app.helpers

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlanId
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveParameters

suspend inline fun ApplicationCall.handleParameters(
    crossinline block: suspend (ProjectPlanId) -> Unit
) {
    val projectId = ProjectPlanId(parameters["projectId"]!!)
    block(projectId)
}