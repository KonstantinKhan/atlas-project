package com.khan366kos.atlas.project.backend.common.models.resource

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.models.project.Project
import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import com.khan366kos.atlas.project.backend.common.models.project.ProjectName
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import kotlinx.datetime.LocalDate

data class ProjectContribution(
    val projectId: String,
    val projectName: String,
    val hours: Double,
)

data class CrossProjectDayLoad(
    val date: LocalDate,
    val totalAssignedHours: Double,
    val capacityHours: Double,
    val projectBreakdown: List<ProjectContribution>,
) {
    val isOverloaded: Boolean get() = totalAssignedHours > capacityHours
}

data class CrossProjectResourceLoad(
    val resourceId: ResourceId,
    val resourceName: String,
    val days: List<CrossProjectDayLoad>,
    val totalAllocatedHours: Double,
) {
    val overloadedDays: List<CrossProjectDayLoad> get() = days.filter { it.isOverloaded }
}

data class CrossProjectOverloadReport(
    val resources: List<CrossProjectResourceLoad>,
    val projects: List<Project>,
) {
    val totalOverloadedDays: Int get() = resources.sumOf { it.overloadedDays.size }
}

data class ProjectLoadInput(
    val projectId: String,
    val projectName: String,
    val portfolioId: String,
    val priority: ProjectPriority,
    val plan: ProjectPlan,
    val assignments: List<TaskAssignment>,
    val dayOverrides: Map<AssignmentId, List<AssignmentDayOverride>>,
)

class CrossProjectLoadAggregator(
    private val projectInputs: List<ProjectLoadInput>,
    private val resources: List<Resource>,
    private val calendar: TimelineCalendar,
    private val calendarOverrides: Map<ResourceId, List<ResourceCalendarOverride>>,
) {
    fun computeLoad(from: LocalDate, to: LocalDate): CrossProjectOverloadReport {
        // Compute per-project load using existing ResourceLoadCalculator
        val perProjectResults = projectInputs.map { input ->
            val calculator = ResourceLoadCalculator(
                plan = input.plan,
                assignments = input.assignments,
                resources = resources,
                calendar = calendar,
                calendarOverrides = calendarOverrides,
                dayOverrides = input.dayOverrides,
            )
            input to calculator.computeLoad(from, to)
        }

        // Merge by (resourceId, date) across all projects
        // Key: resourceId -> date -> list of (projectId, projectName, hours)
        val merged = mutableMapOf<ResourceId, MutableMap<LocalDate, MutableList<ProjectContribution>>>()

        for ((input, report) in perProjectResults) {
            for (resourceResult in report.resources) {
                val dateMap = merged.getOrPut(resourceResult.resourceId) { mutableMapOf() }
                for (dayLoad in resourceResult.days) {
                    val contributions = dateMap.getOrPut(dayLoad.date) { mutableListOf() }
                    if (dayLoad.assignedHours > 0.0) {
                        contributions.add(
                            ProjectContribution(
                                projectId = input.projectId,
                                projectName = input.projectName,
                                hours = dayLoad.assignedHours,
                            )
                        )
                    }
                }
            }
        }

        // Build CrossProjectResourceLoad for each resource
        val crossResourceLoads = resources.mapNotNull { resource ->
            val dateMap = merged[resource.id] ?: return@mapNotNull null

            val days = dateMap.entries
                .sortedBy { it.key }
                .map { (date, contributions) ->
                    val totalAssigned = contributions.sumOf { it.hours }
                    // Get capacity from the per-project results (same for all projects)
                    val capacity = perProjectResults
                        .flatMap { it.second.resources }
                        .find { it.resourceId == resource.id }
                        ?.days?.find { it.date == date }?.capacityHours
                        ?: resource.capacityHoursPerDay

                    CrossProjectDayLoad(
                        date = date,
                        totalAssignedHours = totalAssigned,
                        capacityHours = capacity,
                        projectBreakdown = contributions,
                    )
                }

            if (days.isEmpty()) return@mapNotNull null

            CrossProjectResourceLoad(
                resourceId = resource.id,
                resourceName = resource.name.value,
                days = days,
                totalAllocatedHours = days.sumOf { it.totalAssignedHours },
            )
        }

        val projectInfos = projectInputs.map { input ->
            Project(
                id = ProjectId(input.projectId),
                name = ProjectName(input.projectName),
            )
        }

        return CrossProjectOverloadReport(
            resources = crossResourceLoads,
            projects = projectInfos,
        )
    }
}
