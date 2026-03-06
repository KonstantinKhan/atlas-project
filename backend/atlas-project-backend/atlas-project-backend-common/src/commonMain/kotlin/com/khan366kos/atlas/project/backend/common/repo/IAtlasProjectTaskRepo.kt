package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar

interface IAtlasProjectTaskRepo {
    suspend fun timelineCalendar(): TimelineCalendar

    suspend fun updateSchedule(schedule: TaskSchedule): Int

    suspend fun projectPlan(): ProjectPlan

    suspend fun tasks(): List<ProjectTask>
    suspend fun getTask(id: String): ProjectTask?
    suspend fun createTask(task: ProjectTask): ProjectTask
    suspend fun createTaskWithoutSchedule(task: ProjectTask): ProjectTask
    suspend fun updateTask(task: ProjectTask): ProjectTask

    suspend fun addDependency(predecessorId: String, successorId: String, type: String, lagDays: Int): Int
    suspend fun updateDependencyLag(predecessorId: String, successorId: String, lag: Int): Int
    suspend fun updateDependency(predecessorId: String, successorId: String, type: String, lagDays: Int): Int
    suspend fun deleteDependency(predecessorId: String, successorId: String): Int

    suspend fun deleteTask(id: String): Int
}