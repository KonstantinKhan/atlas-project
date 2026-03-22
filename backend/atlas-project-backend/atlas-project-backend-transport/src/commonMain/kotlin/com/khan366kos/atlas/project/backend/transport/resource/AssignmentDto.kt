package com.khan366kos.atlas.project.backend.transport.resource

import kotlinx.serialization.Serializable

@Serializable
data class TaskAssignmentDto(
    val id: String,
    val taskId: String,
    val resourceId: String,
    val hoursPerDay: Double,
    val plannedEffortHours: Double? = null,
)

@Serializable
data class TaskAssignmentListDto(
    val assignments: List<TaskAssignmentDto>,
)

@Serializable
data class CreateAssignmentCommandDto(
    val taskId: String,
    val resourceId: String,
    val hoursPerDay: Double = 8.0,
    val plannedEffortHours: Double? = null,
)

@Serializable
data class UpdateAssignmentCommandDto(
    val hoursPerDay: Double? = null,
    val plannedEffortHours: Double? = null,
)

@Serializable
data class AssignmentDayOverrideDto(
    val date: String,
    val hours: Double,
)

@Serializable
data class AssignmentDayOverrideListDto(
    val overrides: List<AssignmentDayOverrideDto>,
)

@Serializable
data class SetDayOverrideCommandDto(
    val date: String,
    val hours: Double,
)

@Serializable
data class ResourceDayLoadDto(
    val date: String,
    val assignedHours: Double,
    val capacityHours: Double,
    val isOverloaded: Boolean,
)

@Serializable
data class ResourceLoadResultDto(
    val resourceId: String,
    val resourceName: String,
    val days: List<ResourceDayLoadDto>,
    val overloadedDaysCount: Int,
    val allocatedHours: Double,
    val effortDeficit: Double? = null,
)

@Serializable
data class OverloadReportDto(
    val resources: List<ResourceLoadResultDto>,
    val totalOverloadedDays: Int,
    val totalEffortDeficit: Double,
)
