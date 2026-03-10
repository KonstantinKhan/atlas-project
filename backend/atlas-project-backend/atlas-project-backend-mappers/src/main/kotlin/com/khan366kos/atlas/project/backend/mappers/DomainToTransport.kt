package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
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

fun ProjectPlan.toGanttDto() = GanttProjectPlanDto(
    projectId = id.asString(),
    tasks = tasks().map { task ->
        val schedule = schedules()[TaskScheduleId(task.id.value)]
            ?: TaskSchedule()
        val startDate = (schedule.start as? ProjectDate.Set)?.date
        val endDate = (schedule.end as? ProjectDate.Set)?.date
        GanttTaskDto(
            id = task.id.asString(),
            title = task.title.value,
            description = task.description.value,
            start = startDate,
            end = endDate,
            status = ProjectTaskStatus.valueOf(task.status.name)
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
