package com.khan366kos.atlas.project.backend.transport

import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import kotlinx.serialization.Serializable

@Serializable
data class GanttDependencyDto(
    val fromTaskId: String,
    val toTaskId: String,
    val type: DependencyTypeDto,
    val lagDays: Int = 0
)