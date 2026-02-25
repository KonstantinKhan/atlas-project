package com.khan366kos.atlas.project.backend.common.models

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId

data class TaskDependency(
    val predecessor: TaskId,
    val successor: TaskId,
    val type: DependencyType,
    val lag: Duration = Duration.ZERO
)