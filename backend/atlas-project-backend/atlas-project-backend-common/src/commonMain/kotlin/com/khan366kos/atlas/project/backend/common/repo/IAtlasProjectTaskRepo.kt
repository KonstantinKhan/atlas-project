package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask

interface IAtlasProjectTaskRepo {
    suspend fun tasks(): List<ProjectTask>
}