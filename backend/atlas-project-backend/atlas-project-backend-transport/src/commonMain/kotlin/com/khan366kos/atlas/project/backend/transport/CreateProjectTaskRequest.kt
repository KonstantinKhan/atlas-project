package com.khan366kos.atlas.project.backend.transport

import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectTaskRequest(
    val title: String,
    val description: String = "",
    val plannedCalendarDuration: Int? = null,
    val actualCalendarDuration: Int? = null,
    val plannedStartDate: LocalDate? = null,
    val plannedEndDate: LocalDate? = null,
    val actualStartDate: LocalDate? = null,
    val actualEndDate: LocalDate? = null,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
    val dependsOn: List<String> = emptyList(),
    val dependsOnLag: Map<String, Int> = emptyMap(),
)