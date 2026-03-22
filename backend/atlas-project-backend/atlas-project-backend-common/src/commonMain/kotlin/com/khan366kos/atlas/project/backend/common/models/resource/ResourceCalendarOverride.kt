package com.khan366kos.atlas.project.backend.common.models.resource

import kotlinx.datetime.LocalDate

data class ResourceCalendarOverride(
    val resourceId: ResourceId,
    val date: LocalDate,
    val availableHours: Double,
)
