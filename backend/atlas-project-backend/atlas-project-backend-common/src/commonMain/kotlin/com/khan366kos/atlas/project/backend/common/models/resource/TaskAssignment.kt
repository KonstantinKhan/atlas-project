package com.khan366kos.atlas.project.backend.common.models.resource

import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId

data class TaskAssignment(
    val id: AssignmentId = AssignmentId.NONE,
    val taskId: TaskId,
    val resourceId: ResourceId,
    val hoursPerDay: Double = 8.0,
    val plannedEffortHours: Double? = null,
)
