package com.khan366kos.atlas.project.backend.transport

import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProjectTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val plannedCalendarDuration: Int? = null,
    val actualCalendarDuration: Int? = null,
    val plannedStartDate: LocalDate? = null,
    val plannedEndDate: LocalDate? = null,
    val actualStartDate: LocalDate? = null,
    val actualEndDate: LocalDate? = null,
    val status: ProjectTaskStatus? = null,
    val dependsOn: List<String>? = null,
    val dependsOnLag: Map<String, Int>? = null,
)
