package com.khan366kos.atlas.project.backend.transport

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleUpdateDto(
    val taskId: String,
    val start: LocalDate,
    val end: LocalDate,
)

@Serializable
data class ScheduleDeltaDto(
    val updatedSchedules: List<ScheduleUpdateDto>
)
