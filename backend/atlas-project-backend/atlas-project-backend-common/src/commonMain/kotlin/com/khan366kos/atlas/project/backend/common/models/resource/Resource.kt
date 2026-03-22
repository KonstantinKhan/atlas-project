package com.khan366kos.atlas.project.backend.common.models.resource

data class Resource(
    val id: ResourceId = ResourceId.NONE,
    val name: ResourceName = ResourceName.NONE,
    val type: ResourceType = ResourceType.PERSON,
    val capacityHoursPerDay: Double = 8.0,
    val sortOrder: Int = 0,
)
