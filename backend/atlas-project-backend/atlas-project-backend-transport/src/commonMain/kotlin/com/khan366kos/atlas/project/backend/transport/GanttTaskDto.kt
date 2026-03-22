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
    val status: ProjectTaskStatus,
    val baselineStart: LocalDate? = null,
    val baselineEnd: LocalDate? = null,
    val actualStart: LocalDate? = null,
    val actualEnd: LocalDate? = null,
    val baselineEffortHours: Double? = null,
    val additionalEffortHours: Double? = null,
    val allocatedEffortHours: Double? = null,
    val effortCoveragePercent: Double? = null,
)