package com.khan366kos.atlas.project.backend.transport.calendar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GanttCalendarDto(
    @SerialName("weekendDays")
    val weekendDays: List<Int>,
    @SerialName("holidays")
    val holidays: List<String>,
    @SerialName("workingWeekends")
    val workingWeekends: List<String>,
)
