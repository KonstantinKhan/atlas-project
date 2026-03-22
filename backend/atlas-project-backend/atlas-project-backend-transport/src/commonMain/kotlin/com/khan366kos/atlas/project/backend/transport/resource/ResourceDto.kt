package com.khan366kos.atlas.project.backend.transport.resource

import kotlinx.serialization.Serializable

@Serializable
data class ResourceDto(
    val id: String,
    val name: String,
    val type: String,
    val capacityHoursPerDay: Double,
    val sortOrder: Int = 0,
)

@Serializable
data class ResourceListDto(
    val resources: List<ResourceDto>,
)

@Serializable
data class CreateResourceCommandDto(
    val name: String,
    val type: String = "PERSON",
    val capacityHoursPerDay: Double = 8.0,
)

@Serializable
data class UpdateResourceCommandDto(
    val name: String? = null,
    val type: String? = null,
    val capacityHoursPerDay: Double? = null,
)

@Serializable
data class ResourceCalendarOverrideDto(
    val date: String,
    val availableHours: Double,
)

@Serializable
data class ResourceCalendarOverrideListDto(
    val overrides: List<ResourceCalendarOverrideDto>,
)
