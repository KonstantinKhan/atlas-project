package com.khan366kos.atlas.project.backend.common.models.taskSchedule

import com.khan366kos.atlas.project.backend.common.models.TaskDependency

data class ScheduleDelta(
    val updatedSchedule: List<TaskSchedule>,
    val updatedDependencies: List<TaskDependency> = emptyList(),
)
