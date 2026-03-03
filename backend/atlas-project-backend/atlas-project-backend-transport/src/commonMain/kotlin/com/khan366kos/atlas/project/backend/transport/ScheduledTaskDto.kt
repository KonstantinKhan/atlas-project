package com.khan366kos.atlas.project.backend.transport

import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledTaskDto(
    val id: String,
    val title: String,
    val description: String = "",
    val plannedStartDate: LocalDate,
    val plannedEndDate: LocalDate,
    val plannedCalendarDuration: Int? = null,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
    val dependsOn: List<String> = emptyList(),
    val dependsOnLag: Map<String, Int> = emptyMap(),
)
