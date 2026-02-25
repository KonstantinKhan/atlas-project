package com.khan366kos.atlas.project.backend.transport.ganttProjectPlan

import com.khan366kos.atlas.project.backend.transport.GanttDependencyDto
import com.khan366kos.atlas.project.backend.transport.GanttTaskDto
import kotlinx.serialization.Serializable

@Serializable
data class GanttProjectPlanDto(
    val projectId: String,
    val tasks: List<GanttTaskDto>,
    val dependencies: List<GanttDependencyDto>
)
