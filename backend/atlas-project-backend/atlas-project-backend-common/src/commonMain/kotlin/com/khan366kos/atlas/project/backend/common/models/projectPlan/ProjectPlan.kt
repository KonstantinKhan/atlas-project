package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId

data class ProjectPlan(
    val id: ProjectPlanId = ProjectPlanId.NONE,
    private val tasks: MutableMap<TaskId, ProjectTask> = mutableMapOf(),
    private val schedules: MutableMap<TaskScheduleId, TaskSchedule>,
    private val dependencies: MutableSet<TaskDependency> = mutableSetOf(),
) {
    fun tasks() = tasks.values.toList()
    fun schedules() = schedules
    fun dependencies() = dependencies
}