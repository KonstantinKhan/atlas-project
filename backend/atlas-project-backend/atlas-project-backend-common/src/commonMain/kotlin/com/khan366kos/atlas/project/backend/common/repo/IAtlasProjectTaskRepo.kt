package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar

interface IAtlasProjectTaskRepo {
    suspend fun timelineCalendar(): TimelineCalendar

    suspend fun tasks(): List<ProjectTask>
    suspend fun getTask(id: String): ProjectTask?
    suspend fun createTask(task: ProjectTask): ProjectTask
    suspend fun updateTask(task: ProjectTask): ProjectTask
}