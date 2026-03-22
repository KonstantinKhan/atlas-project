package com.khan366kos.atlas.project.backend.transport.resource

import com.khan366kos.atlas.project.backend.transport.ScheduleUpdateDto
import kotlinx.serialization.Serializable

@Serializable
data class LevelingResultDto(
    val updatedSchedules: List<ScheduleUpdateDto>,
    val resolvedOverloads: Int,
    val remainingOverloads: Int,
)
