package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.transport.GanttDependencyDto
import com.khan366kos.atlas.project.backend.transport.GanttTaskDto
import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.transport.ganttProjectPlan.GanttProjectPlanDto
import com.khan366kos.atlas.project.backend.transport.timelineCalendar.TimelineCalendarDto

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
            ?: error("No schedule for task ${task.id.value}")
        val startDate = (schedule.start as? ProjectDate.Set)?.date
            ?: error("Schedule start is not ProjectDate.Set for task ${task.id.value}")
        val endDate = (schedule.end as? ProjectDate.Set)?.date
            ?: error("Schedule end is not ProjectDate.Set for task ${task.id.value}")
        GanttTaskDto(
            id = task.id.asString(),
            title = task.title.value,
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

