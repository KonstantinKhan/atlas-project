package com.khan366kos.atlas.project.backend.common.models.resource

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.projectPlan.CriticalPathAnalysis
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.projectPlan.topologicalSort
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.ScheduleDelta
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class LevelingResult(
    val scheduleDelta: ScheduleDelta,
    val resolvedOverloads: Int,
    val remainingOverloads: Int,
)

class ResourceLevelingEngine(
    private val plan: ProjectPlan,
    private val assignments: List<TaskAssignment>,
    private val resources: List<Resource>,
    private val calendar: TimelineCalendar,
    private val calendarOverrides: Map<ResourceId, List<ResourceCalendarOverride>>,
    private val dayOverrides: Map<AssignmentId, List<AssignmentDayOverride>> = emptyMap(),
) {
    private val maxIterations = 100

    fun level(): LevelingResult {
        val snapshot = plan.snapshot()
        val originalSchedules = plan.schedules().toMap()

        val initialReport = computeLoad(snapshot)
        val initialOverloads = initialReport.totalOverloadedDays

        var iteration = 0
        while (iteration < maxIterations) {
            iteration++

            val report = computeLoad(snapshot)
            if (report.totalOverloadedDays == 0) break

            val overloadedDay = findEarliestOverloadedDay(report) ?: break
            val resourceId = overloadedDay.first
            val date = overloadedDay.second

            val taskIdsOnDay = findTasksOnDay(snapshot, resourceId, date)
            if (taskIdsOnDay.size <= 1) break

            val cpmResult = CriticalPathAnalysis(snapshot, calendar).compute()

            val taskToShift = taskIdsOnDay
                .mapNotNull { taskId ->
                    val cpm = cpmResult.tasks[taskId] ?: return@mapNotNull null
                    taskId to cpm.slack
                }
                .maxByOrNull { it.second }
                ?.first
                ?: break

            val nextStart = findNextAvailableDay(snapshot, resourceId, date)
            snapshot.changeTaskStartDate(taskToShift, nextStart, calendar)
        }

        val finalReport = computeLoad(snapshot)
        val resolvedOverloads = maxOf(0, initialOverloads - finalReport.totalOverloadedDays)

        val updatedSchedules = mutableListOf<TaskSchedule>()
        for ((schedId, newSchedule) in snapshot.schedules()) {
            val original = originalSchedules[schedId]
            if (original == null || original.start != newSchedule.start || original.end != newSchedule.end) {
                updatedSchedules.add(newSchedule)
            }
        }

        return LevelingResult(
            scheduleDelta = ScheduleDelta(updatedSchedule = updatedSchedules),
            resolvedOverloads = resolvedOverloads,
            remainingOverloads = finalReport.totalOverloadedDays,
        )
    }

    private fun computeLoad(snapshot: ProjectPlan): OverloadReport {
        val dateRange = computeDateRange(snapshot) ?: return OverloadReport(emptyList())
        val calculator = ResourceLoadCalculator(snapshot, assignments, resources, calendar, calendarOverrides, dayOverrides)
        return calculator.computeLoad(dateRange.first, dateRange.second)
    }

    private fun computeDateRange(snapshot: ProjectPlan): Pair<LocalDate, LocalDate>? {
        val schedules = snapshot.schedules()
        val dates = schedules.values.mapNotNull { schedule ->
            val start = (schedule.start as? ProjectDate.Set)?.date ?: return@mapNotNull null
            val end = (schedule.end as? ProjectDate.Set)?.date ?: return@mapNotNull null
            start to end
        }
        if (dates.isEmpty()) return null
        val minStart = dates.minOf { it.first }
        val maxEnd = dates.maxOf { it.second }
        return minStart to maxEnd
    }

    private fun findEarliestOverloadedDay(report: OverloadReport): Pair<ResourceId, LocalDate>? {
        var earliest: Pair<ResourceId, LocalDate>? = null
        for (resourceResult in report.resources) {
            for (day in resourceResult.overloadedDays) {
                if (earliest == null || day.date < earliest.second) {
                    earliest = resourceResult.resourceId to day.date
                }
            }
        }
        return earliest
    }

    private fun findTasksOnDay(
        snapshot: ProjectPlan,
        resourceId: ResourceId,
        date: LocalDate,
    ): List<TaskId> {
        val resourceAssignments = assignments.filter { it.resourceId == resourceId }
        return resourceAssignments.mapNotNull { assignment ->
            val schedule = snapshot.schedules()[TaskScheduleId(assignment.taskId.value)] ?: return@mapNotNull null
            val start = (schedule.start as? ProjectDate.Set)?.date ?: return@mapNotNull null
            val end = (schedule.end as? ProjectDate.Set)?.date ?: return@mapNotNull null
            if (date in start..end) assignment.taskId else null
        }
    }

    private fun findNextAvailableDay(
        snapshot: ProjectPlan,
        resourceId: ResourceId,
        overloadedDate: LocalDate,
    ): LocalDate {
        var candidate = overloadedDate.plus(DatePeriod(days = 1))
        val maxSearch = 365
        var searched = 0
        while (searched < maxSearch) {
            if (calendar.isWorkingDay(candidate)) {
                val calculator = ResourceLoadCalculator(snapshot, assignments, resources, calendar, calendarOverrides, dayOverrides)
                val dayLoad = calculator.computeResourceLoad(resourceId, candidate, candidate)
                val day = dayLoad.days.firstOrNull()
                if (day == null || !day.isOverloaded) {
                    return candidate
                }
            }
            candidate = candidate.plus(DatePeriod(days = 1))
            searched++
        }
        return candidate
    }
}
