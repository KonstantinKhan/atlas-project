package com.khan366kos.atlas.project.backend.transport.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignScheduleCommandDto(
    @SerialName("start")
    val start: String,
    @SerialName("duration")
    val duration: Int
)
