package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentDayOverride
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectDayLoad
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectOverloadReport
import com.khan366kos.atlas.project.backend.common.models.resource.CrossProjectResourceLoad
import com.khan366kos.atlas.project.backend.common.models.resource.LevelingResult
import com.khan366kos.atlas.project.backend.common.models.resource.OverloadReport
import com.khan366kos.atlas.project.backend.common.models.resource.ProjectContribution
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
import com.khan366kos.atlas.project.backend.common.models.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
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
import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto
import com.khan366kos.atlas.project.backend.transport.ganttProjectPlan.GanttProjectPlanDto
import com.khan366kos.atlas.project.backend.transport.project.ProjectDto
import com.khan366kos.atlas.project.backend.transport.resource.CrossProjectDayLoadDto
import com.khan366kos.atlas.project.backend.transport.resource.CrossProjectOverloadReportDto
import com.khan366kos.atlas.project.backend.transport.resource.CrossProjectResourceLoadDto
import com.khan366kos.atlas.project.backend.transport.resource.LevelingResultDto
import com.khan366kos.atlas.project.backend.transport.resource.OverloadReportDto
import com.khan366kos.atlas.project.backend.transport.resource.ProjectContributionDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceCalendarOverrideDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceDayLoadDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceDto
import com.khan366kos.atlas.project.backend.transport.resource.AssignmentDayOverrideDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceLoadResultDto
import com.khan366kos.atlas.project.backend.transport.resource.TaskAssignmentDto
import com.khan366kos.atlas.project.backend.common.models.user.User
import com.khan366kos.atlas.project.backend.transport.responses.PortfolioResponseDto
import com.khan366kos.atlas.project.backend.transport.user.UserResponseDto
import com.khan366kos.atlas.project.backend.transport.responses.project.ProjectResponseDto
import com.khan366kos.atlas.project.backend.transport.timelineCalendar.TimelineCalendarDto

fun ScheduleDelta.toUpdatableProjectDto() = ScheduleDeltaDto(
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
        val allocatedEffortHours =
            if (calendar != null && startDate != null && endDate != null && taskAssignments.isNotEmpty()) {
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

fun CriticalPathResult.toUpdatableProjectDto() = CriticalPathDto(
    tasks = tasks.values.map { it.toUpdatableProjectDto() },
    criticalTaskIds = criticalTaskIds.map { it.value },
    projectEnd = projectEnd,
)

fun CpmTaskResult.toUpdatableProjectDto() = CpmTaskDto(
    taskId = taskId.value,
    es = es,
    ef = ef,
    ls = ls,
    lf = lf,
    slack = slack,
    isCritical = isCritical,
)

fun BlockerChainResult.toUpdatableProjectDto() = BlockerChainDto(
    targetTaskId = targetTaskId.value,
    blockers = blockers.map { it.toUpdatableProjectDto() },
)

fun BlockerInfo.toUpdatableProjectDto() = BlockerInfoDto(
    taskId = taskId.value,
    title = title,
    status = status.value,
    start = start,
    end = end,
    depth = depth,
)

fun AvailableTasksResult.toUpdatableProjectDto() = AvailableTasksDto(
    tasks = tasks.map { it.toUpdatableProjectDto() },
    asOfDate = asOfDate,
)

fun AvailableTaskInfo.toUpdatableProjectDto() = AvailableTaskInfoDto(
    taskId = taskId.value,
    title = title,
    status = status.value,
    start = start,
    end = end,
)

fun WhatIfResult.toUpdatableProjectDto() = WhatIfDto(
    movedTaskId = movedTaskId.value,
    impacts = impacts.map { it.toUpdatableProjectDto() },
)

fun TaskImpact.toUpdatableProjectDto() = TaskImpactDto(
    taskId = taskId.value,
    title = title,
    oldStart = oldStart,
    oldEnd = oldEnd,
    newStart = newStart,
    newEnd = newEnd,
    deltaStartDays = deltaStartDays,
    deltaEndDays = deltaEndDays,
)

fun Resource.toUpdatableProjectDto() = ResourceDto(
    id = id.value,
    name = name.value,
    type = type.name,
    capacityHoursPerDay = capacityHoursPerDay,
    sortOrder = sortOrder,
)

fun ResourceCalendarOverride.toUpdatableProjectDto() = ResourceCalendarOverrideDto(
    date = date.toString(),
    availableHours = availableHours,
)

fun TaskAssignment.toUpdatableProjectDto() = TaskAssignmentDto(
    id = id.value,
    taskId = taskId.value,
    resourceId = resourceId.value,
    hoursPerDay = hoursPerDay,
    plannedEffortHours = plannedEffortHours,
)

fun AssignmentDayOverride.toUpdatableProjectDto() = AssignmentDayOverrideDto(
    date = date.toString(),
    hours = hours,
)

fun ResourceDayLoad.toUpdatableProjectDto() = ResourceDayLoadDto(
    date = date.toString(),
    assignedHours = assignedHours,
    capacityHours = capacityHours,
    isOverloaded = isOverloaded,
)

fun ResourceLoadResult.toUpdatableProjectDto() = ResourceLoadResultDto(
    resourceId = resourceId.value,
    resourceName = resourceName,
    days = days.map { it.toUpdatableProjectDto() },
    overloadedDaysCount = overloadedDays.size,
    allocatedHours = allocatedHours,
    effortDeficit = effortDeficit,
)

fun LevelingResult.toUpdatableProjectDto() = LevelingResultDto(
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

fun OverloadReport.toUpdatableProjectDto() = OverloadReportDto(
    resources = resources.map { it.toUpdatableProjectDto() },
    totalOverloadedDays = totalOverloadedDays,
    totalEffortDeficit = totalEffortDeficit,
)

fun ProjectContribution.toUpdatableProjectDto() = ProjectContributionDto(
    projectId = projectId,
    projectName = projectName,
    hours = hours,
)

fun CrossProjectDayLoad.toUpdatableProjectDto() = CrossProjectDayLoadDto(
    date = date.toString(),
    totalAssignedHours = totalAssignedHours,
    capacityHours = capacityHours,
    isOverloaded = isOverloaded,
    projectBreakdown = projectBreakdown.map { it.toUpdatableProjectDto() },
)

fun CrossProjectResourceLoad.toUpdatableProjectDto() = CrossProjectResourceLoadDto(
    resourceId = resourceId.value,
    resourceName = resourceName,
    days = days.map { it.toUpdatableProjectDto() },
    overloadedDaysCount = overloadedDays.size,
    totalAllocatedHours = totalAllocatedHours,
)

fun CrossProjectOverloadReport.toUpdatableProjectDto() = CrossProjectOverloadReportDto(
    resources = resources.map { it.toUpdatableProjectDto() },
    projects = projects.map { it.toUpdatableProjectDto() },
    totalOverloadedDays = totalOverloadedDays,
)

fun ProjectPriority.toUpdatableProjectDto() =
    ProjectPriorityDto.valueOf(this.name)

fun Project.toUpdatableProjectDto() = ProjectDto(
    id = id.asString(),
    name = name.asString(),
)

fun Portfolio.toResponsePortfolioDto() = PortfolioResponseDto(
    id = id.asString(),
    name = name.value,
    description = description.value,
)

fun PortfolioProject.toResponseProjectDto() = ProjectResponseDto(
    id = id.asString(),
    name = name.asString(),
    priority = ProjectPriorityDto.valueOf(priority.name),
)

fun User.toDto(): UserResponseDto = UserResponseDto(
    id = this.id.asString(),
    name = this.name.value,
    age = this.age.value,
    role = this.role.name,
)