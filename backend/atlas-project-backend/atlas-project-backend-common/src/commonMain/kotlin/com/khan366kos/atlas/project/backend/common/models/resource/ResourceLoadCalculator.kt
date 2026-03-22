package com.khan366kos.atlas.project.backend.common.models.resource

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class ResourceDayLoad(
    val date: LocalDate,
    val assignedHours: Double,
    val capacityHours: Double,
) {
    val isOverloaded: Boolean get() = assignedHours > capacityHours
}

data class ResourceLoadResult(
    val resourceId: ResourceId,
    val resourceName: String,
    val days: List<ResourceDayLoad>,
    val allocatedHours: Double,
    val effortDeficit: Double?,
) {
    val overloadedDays: List<ResourceDayLoad> get() = days.filter { it.isOverloaded }
}

data class OverloadReport(
    val resources: List<ResourceLoadResult>,
) {
    val totalOverloadedDays: Int get() = resources.sumOf { it.overloadedDays.size }
    val totalEffortDeficit: Double get() = resources.mapNotNull { it.effortDeficit }.sum()
}

class ResourceLoadCalculator(
    private val plan: ProjectPlan,
    private val assignments: List<TaskAssignment>,
    private val resources: List<Resource>,
    private val calendar: TimelineCalendar,
    private val calendarOverrides: Map<ResourceId, List<ResourceCalendarOverride>>,
    private val dayOverrides: Map<AssignmentId, List<AssignmentDayOverride>> = emptyMap(),
) {
    fun computeLoad(from: LocalDate, to: LocalDate): OverloadReport {
        val results = resources.map { resource ->
            computeResourceLoad(resource, from, to)
        }
        return OverloadReport(resources = results)
    }

    fun computeResourceLoad(resourceId: ResourceId, from: LocalDate, to: LocalDate): ResourceLoadResult {
        val resource = resources.find { it.id == resourceId }
            ?: return ResourceLoadResult(resourceId, "", emptyList(), 0.0, null)
        return computeResourceLoad(resource, from, to)
    }

    private fun computeResourceLoad(resource: Resource, from: LocalDate, to: LocalDate): ResourceLoadResult {
        val resourceAssignments = assignments.filter { it.resourceId == resource.id }
        if (resourceAssignments.isEmpty()) {
            return ResourceLoadResult(resource.id, resource.name.value, emptyList(), 0.0, null)
        }

        val overrides = calendarOverrides[resource.id] ?: emptyList()
        val overrideMap = overrides.associateBy { it.date }

        val assignmentDayOverrideMaps = resourceAssignments.associate { assignment ->
            assignment.id to (dayOverrides[assignment.id]?.associateBy { it.date } ?: emptyMap())
        }

        val days = mutableListOf<ResourceDayLoad>()
        val assignmentAllocated = mutableMapOf<AssignmentId, Double>()

        var current = from
        while (current <= to) {
            if (calendar.isWorkingDay(current)) {
                val capacityForDay = overrideMap[current]?.availableHours ?: resource.capacityHoursPerDay
                val assignedHours = resourceAssignments.sumOf { assignment ->
                    if (isTaskScheduledOnDay(assignment.taskId.value, current)) {
                        val dayOverrideMap = assignmentDayOverrideMaps[assignment.id] ?: emptyMap()
                        val hoursForDay = dayOverrideMap[current]?.hours ?: assignment.hoursPerDay
                        assignmentAllocated[assignment.id] = (assignmentAllocated[assignment.id] ?: 0.0) + hoursForDay
                        hoursForDay
                    } else {
                        0.0
                    }
                }
                if (assignedHours > 0.0 || capacityForDay != resource.capacityHoursPerDay) {
                    days.add(ResourceDayLoad(current, assignedHours, capacityForDay))
                }
            }
            current = current.plus(DatePeriod(days = 1))
        }

        val allocatedHours = assignmentAllocated.values.sum()

        val effortDeficit = resourceAssignments
            .filter { it.plannedEffortHours != null }
            .takeIf { it.isNotEmpty() }
            ?.sumOf { assignment ->
                val allocated = assignmentAllocated[assignment.id] ?: 0.0
                val planned = assignment.plannedEffortHours!!
                (planned - allocated).coerceAtLeast(0.0)
            }

        return ResourceLoadResult(resource.id, resource.name.value, days, allocatedHours, effortDeficit)
    }

    private fun isTaskScheduledOnDay(taskId: String, day: LocalDate): Boolean {
        val schedule = plan.schedules()[TaskScheduleId(taskId)] ?: return false
        val start = (schedule.start as? ProjectDate.Set)?.date ?: return false
        val end = (schedule.end as? ProjectDate.Set)?.date ?: return false
        return day in start..end
    }
}
