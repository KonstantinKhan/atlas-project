package com.khan366kos.atlas.project.backend.project.service

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlanId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo

class ProjectService(
    private val projectRepo: IAtlasProjectTaskRepo
) {
    suspend fun project(projectId: ProjectPlanId) = projectRepo.projectPlan(projectId.asString())
}