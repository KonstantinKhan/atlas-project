package com.khan366kos.atlas.project.backend.ktor.app.helpers

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlanId
import io.ktor.server.application.ApplicationCall

suspend inline fun ApplicationCall.handleProjectId(
    crossinline block: suspend (ProjectPlanId) -> Unit
) {
    val projectId = ProjectPlanId(parameters["projectId"]!!)
    block(projectId)
}