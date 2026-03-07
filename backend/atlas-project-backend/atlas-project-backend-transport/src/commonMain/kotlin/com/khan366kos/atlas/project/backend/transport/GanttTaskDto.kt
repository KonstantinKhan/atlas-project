package com.khan366kos.atlas.project.backend.transport

import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class GanttTaskDto(
    val id: String,
    val title: String,
    val description: String,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val status: ProjectTaskStatus
)