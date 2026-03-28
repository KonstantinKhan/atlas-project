package com.khan366kos.atlas.project.backend.transport.resource

import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import com.khan366kos.atlas.project.backend.transport.project.ProjectDto
import kotlinx.serialization.Serializable

@Serializable
data class ProjectContributionDto(
    val projectId: String,
    val projectName: String,
    val hours: Double,
)

@Serializable
data class CrossProjectDayLoadDto(
    val date: String,
    val totalAssignedHours: Double,
    val capacityHours: Double,
    val isOverloaded: Boolean,
    val projectBreakdown: List<ProjectContributionDto>,
)

@Serializable
data class CrossProjectResourceLoadDto(
    val resourceId: String,
    val resourceName: String,
    val days: List<CrossProjectDayLoadDto>,
    val overloadedDaysCount: Int,
    val totalAllocatedHours: Double,
)

@Serializable
data class CrossProjectOverloadReportDto(
    val resources: List<CrossProjectResourceLoadDto>,
    val projects: List<ProjectDto>,
    val totalOverloadedDays: Int,
)
