package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentDayOverride
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectDayLoad
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectOverloadReport
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectResourceLoad
import com.khan366kos.atlas.project.backend.common.models.resource.LevelingResult
import com.khan366kos.atlas.project.backend.common.models.resource.OverloadReport
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectContribution
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectInfo
import com.khan366kos.atlas.project.backend.common.models.resource.Resource
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceCalendarOverride
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceDayLoad
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceLoadResult
import com.khan366kos.atlas.project.backend.common.models.resource.TaskAssignment
import com.khan366kos.atlas.project.backend.common.models.projectPlan.AvailableTaskInfo
import com.khan366kos.atlas.project.backend.common.models.projectPlan.AvailableTasksResult
import com.khan366kos.atlas.project.backend.common.models.projectPlan.BlockerChainResult
import com.khan366kos.atlas.project.backend.common.models.projectPlan.BlockerInfo
import com.khan366kos.atlas.project.backend.common.models.projectPlan.CpmTaskResult
import com.khan366kos.atlas.project.backend.common.models.projectPlan.CriticalPathResult
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.projectPlan.TaskImpact
import com.khan366kos.atlas.project.backend.common.models.projectPlan.WhatIfResult
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.ScheduleDelta
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.transport.GanttDependencyDto
import com.khan366kos.atlas.project.backend.transport.GanttTaskDto
import com.khan366kos.atlas.project.backend.transport.ScheduleDeltaDto
import com.khan366kos.atlas.project.backend.transport.ScheduleUpdateDto
import com.khan366kos.atlas.project.backend.transport.analysis.AvailableTaskInfoDto
import com.khan366kos.atlas.project.backend.transport.analysis.AvailableTasksDto
import com.khan366kos.atlas.project.backend.transport.analysis.BlockerChainDto
import com.khan366kos.atlas.project.backend.transport.analysis.BlockerInfoDto
import com.khan366kos.atlas.project.backend.transport.analysis.TaskImpactDto
import com.khan366kos.atlas.project.backend.transport.analysis.WhatIfDto
import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.transport.cpm.CpmTaskDto
import com.khan366kos.atlas.project.backend.transport.cpm.CriticalPathDto
import com.khan366kos.atlas.project.backend.transport.ganttProjectPlan.GanttProjectPlanDto
import com.khan366kos.atlas.project.backend.transport.resource.CrossProjectDayLoadDto
import com.khan366kos.atlas.project.backend.transport.resource.CrossProjectOverloadReportDto
import com.khan366kos.atlas.project.backend.transport.resource.CrossProjectResourceLoadDto
import com.khan366kos.atlas.project.backend.transport.resource.LevelingResultDto
import com.khan366kos.atlas.project.backend.transport.resource.OverloadReportDto
import com.khan366kos.atlas.project.backend.transport.resource.ProjectContributionDto
import com.khan366kos.atlas.project.backend.transport.resource.ProjectInfoDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceCalendarOverrideDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceDayLoadDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceDto
import com.khan366kos.atlas.project.backend.transport.resource.AssignmentDayOverrideDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceLoadResultDto
import com.khan366kos.atlas.project.backend.transport.resource.TaskAssignmentDto
import com.khan366kos.atlas.project.backend.transport.timelineCalendar.TimelineCalendarDto

fun ScheduleDelta.toDto() = ScheduleDeltaDto(
    updatedSchedules = updatedSchedule.map { schedule ->
        ScheduleUpdateDto(
            taskId = schedule.id.value,
            start = (schedule.start as ProjectDate.Set).date,
            end = (schedule.end as ProjectDate.Set).date,
        )
    }
)

fun TimelineCalendar.toTransport() = TimelineCalendarDto(
    workingWeekDays = workingWeekDays,
    weekendWeekDays = weekendWeekDays,
    holidays = holidays,
    workingWeekends = workingWeekends
)

fun ProjectPlan.toGanttDto(
    assignments: List<TaskAssignment> = emptyList(),
    calendar: TimelineCalendar? = null,
) = GanttProjectPlanDto(
    projectId = id.asString(),
    tasks = tasks().map { task ->
        val schedule = schedules()[TaskScheduleId(task.id.value)]
            ?: TaskSchedule()
        val startDate = (schedule.start as? ProjectDate.Set)?.date
        val endDate = (schedule.end as? ProjectDate.Set)?.date

        val taskAssignments = assignments.filter { it.taskId.value == task.id.value }
        val allocatedEffortHours = if (calendar != null && startDate != null && endDate != null && taskAssignments.isNotEmpty()) {
            val workDays = calendar.workingDaysBetween(startDate, endDate).asInt()
            taskAssignments.sumOf { it.hoursPerDay * workDays }
        } else {
            null
        }
        val totalPlanned = (task.baselineEffortHours ?: 0.0) + (task.additionalEffortHours ?: 0.0)
        val effortCoveragePercent = if (totalPlanned > 0 && allocatedEffortHours != null) {
            allocatedEffortHours / totalPlanned * 100.0
        } else {
            null
        }

        GanttTaskDto(
            id = task.id.asString(),
            title = task.title.value,
            description = task.description.value,
            start = startDate,
            end = endDate,
            status = ProjectTaskStatus.valueOf(task.status.name),
            baselineStart = task.baselineStart,
            baselineEnd = task.baselineEnd,
            actualStart = task.actualStart,
            actualEnd = task.actualEnd,
            baselineEffortHours = task.baselineEffortHours,
            additionalEffortHours = task.additionalEffortHours,
            allocatedEffortHours = allocatedEffortHours,
            effortCoveragePercent = effortCoveragePercent,
        )
    },
    dependencies = dependencies().map {
        GanttDependencyDto(
            fromTaskId = it.predecessor.value,
            toTaskId = it.successor.value,
            type = DependencyTypeDto.valueOf(it.type.name),
            lagDays = it.lag.asInt()
        )
    }
)

fun CriticalPathResult.toDto() = CriticalPathDto(
    tasks = tasks.values.map { it.toDto() },
    criticalTaskIds = criticalTaskIds.map { it.value },
    projectEnd = projectEnd,
)

fun CpmTaskResult.toDto() = CpmTaskDto(
    taskId = taskId.value,
    es = es,
    ef = ef,
    ls = ls,
    lf = lf,
    slack = slack,
    isCritical = isCritical,
)

fun BlockerChainResult.toDto() = BlockerChainDto(
    targetTaskId = targetTaskId.value,
    blockers = blockers.map { it.toDto() },
)

fun BlockerInfo.toDto() = BlockerInfoDto(
    taskId = taskId.value,
    title = title,
    status = status.value,
    start = start,
    end = end,
    depth = depth,
)

fun AvailableTasksResult.toDto() = AvailableTasksDto(
    tasks = tasks.map { it.toDto() },
    asOfDate = asOfDate,
)

fun AvailableTaskInfo.toDto() = AvailableTaskInfoDto(
    taskId = taskId.value,
    title = title,
    status = status.value,
    start = start,
    end = end,
)

fun WhatIfResult.toDto() = WhatIfDto(
    movedTaskId = movedTaskId.value,
    impacts = impacts.map { it.toDto() },
)

fun TaskImpact.toDto() = TaskImpactDto(
    taskId = taskId.value,
    title = title,
    oldStart = oldStart,
    oldEnd = oldEnd,
    newStart = newStart,
    newEnd = newEnd,
    deltaStartDays = deltaStartDays,
    deltaEndDays = deltaEndDays,
)

fun Resource.toDto() = ResourceDto(
    id = id.value,
    name = name.value,
    type = type.name,
    capacityHoursPerDay = capacityHoursPerDay,
    sortOrder = sortOrder,
)

fun ResourceCalendarOverride.toDto() = ResourceCalendarOverrideDto(
    date = date.toString(),
    availableHours = availableHours,
)

fun TaskAssignment.toDto() = TaskAssignmentDto(
    id = id.value,
    taskId = taskId.value,
    resourceId = resourceId.value,
    hoursPerDay = hoursPerDay,
    plannedEffortHours = plannedEffortHours,
)

fun AssignmentDayOverride.toDto() = AssignmentDayOverrideDto(
    date = date.toString(),
    hours = hours,
)

fun ResourceDayLoad.toDto() = ResourceDayLoadDto(
    date = date.toString(),
    assignedHours = assignedHours,
    capacityHours = capacityHours,
    isOverloaded = isOverloaded,
)

fun ResourceLoadResult.toDto() = ResourceLoadResultDto(
    resourceId = resourceId.value,
    resourceName = resourceName,
    days = days.map { it.toDto() },
    overloadedDaysCount = overloadedDays.size,
    allocatedHours = allocatedHours,
    effortDeficit = effortDeficit,
)

fun LevelingResult.toDto() = LevelingResultDto(
    updatedSchedules = scheduleDelta.updatedSchedule.map { schedule ->
        ScheduleUpdateDto(
            taskId = schedule.id.value,
            start = (schedule.start as ProjectDate.Set).date,
            end = (schedule.end as ProjectDate.Set).date,
        )
    },
    resolvedOverloads = resolvedOverloads,
    remainingOverloads = remainingOverloads,
)

fun OverloadReport.toDto() = OverloadReportDto(
    resources = resources.map { it.toDto() },
    totalOverloadedDays = totalOverloadedDays,
    totalEffortDeficit = totalEffortDeficit,
)

fun ProjectContribution.toDto() = ProjectContributionDto(
    projectId = projectId,
    projectName = projectName,
    hours = hours,
)

fun CrossProjectDayLoad.toDto() = CrossProjectDayLoadDto(
    date = date.toString(),
    totalAssignedHours = totalAssignedHours,
    capacityHours = capacityHours,
    isOverloaded = isOverloaded,
    projectBreakdown = projectBreakdown.map { it.toDto() },
)

fun CrossProjectResourceLoad.toDto() = CrossProjectResourceLoadDto(
    resourceId = resourceId.value,
    resourceName = resourceName,
    days = days.map { it.toDto() },
    overloadedDaysCount = overloadedDays.size,
    totalAllocatedHours = totalAllocatedHours,
)

fun ProjectInfo.toDto() = ProjectInfoDto(
    id = id,
    name = name,
    priority = priority.toDto(),
    portfolioId = portfolioId,
)

fun CrossProjectOverloadReport.toDto() = CrossProjectOverloadReportDto(
    resources = resources.map { it.toDto() },
    projects = projects.map { it.toDto() },
    totalOverloadedDays = totalOverloadedDays,
)

fun com.khan366kos.atlas.project.backend.common.project.ProjectPriority.toDto() =
    com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto.valueOf(this.name)
