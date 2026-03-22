package com.khan366kos.atlas.project.backend.transport

import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProjectTaskRequest(
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("plannedCalendarDuration")
    val plannedCalendarDuration: Int? = null,
    @SerialName("actualCalendarDuration")
    val actualCalendarDuration: Int? = null,
    @SerialName("plannedStartDate")
    val plannedStartDate: LocalDate? = null,
    @SerialName("plannedEndDate")
    val plannedEndDate: LocalDate? = null,
    @SerialName("actualStartDate")
    val actualStartDate: LocalDate? = null,
    @SerialName("actualEndDate")
    val actualEndDate: LocalDate? = null,
    @SerialName("baselineEffortHours")
    val baselineEffortHours: Double? = null,
    @SerialName("additionalEffortHours")
    val additionalEffortHours: Double? = null,
    @SerialName("status")
    val status: ProjectTaskStatus? = null,
    @SerialName("dependsOn")
    val dependsOn: List<String>? = null,
    @SerialName("dependsOnLag")
    val dependsOnLag: Map<String, Int>? = null,
)
