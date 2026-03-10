package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

data class BlockerInfo(
    val taskId: TaskId,
    val title: String,
    val status: ProjectTaskStatus,
    val start: LocalDate?,
    val end: LocalDate?,
    val depth: Int,
)

data class BlockerChainResult(
    val targetTaskId: TaskId,
    val blockers: List<BlockerInfo>,
)

data class AvailableTaskInfo(
    val taskId: TaskId,
    val title: String,
    val status: ProjectTaskStatus,
    val start: LocalDate,
    val end: LocalDate,
)

data class AvailableTasksResult(
    val tasks: List<AvailableTaskInfo>,
    val asOfDate: LocalDate,
)

data class TaskImpact(
    val taskId: TaskId,
    val title: String,
    val oldStart: LocalDate,
    val oldEnd: LocalDate,
    val newStart: LocalDate,
    val newEnd: LocalDate,
    val deltaStartDays: Int,
    val deltaEndDays: Int,
)

data class WhatIfResult(
    val movedTaskId: TaskId,
    val impacts: List<TaskImpact>,
)

fun ProjectPlan.blockerChain(taskId: TaskId): BlockerChainResult {
    val depthMap = mutableMapOf<TaskId, Int>()
    val visited = mutableSetOf<TaskId>()
    val queue = ArrayDeque<Pair<TaskId, Int>>()

    val immediatePreds = dependencies().filter { it.successor == taskId }.map { it.predecessor }
    for (pred in immediatePreds) {
        if (pred !in visited) {
            visited.add(pred)
            depthMap[pred] = 1
            queue.add(pred to 1)
        }
    }

    while (queue.isNotEmpty()) {
        val (currentId, currentDepth) = queue.removeFirst()
        val preds = dependencies().filter { it.successor == currentId }.map { it.predecessor }
        for (pred in preds) {
            if (pred !in visited) {
                visited.add(pred)
                depthMap[pred] = currentDepth + 1
                queue.add(pred to currentDepth + 1)
            }
        }
    }

    if (visited.isEmpty()) {
        return BlockerChainResult(targetTaskId = taskId, blockers = emptyList())
    }

    val subgraphDeps = dependencies().filter { it.predecessor in visited && it.successor in visited }
    val sorted = topologicalSort(visited, subgraphDeps)

    val tasksByIdMap = tasks().associateBy { it.id }
    val blockers = sorted.map { id ->
        val task = tasksByIdMap[id]
        val schedule = schedules()[TaskScheduleId(id.value)]
        val start = (schedule?.start as? ProjectDate.Set)?.date
        val end = (schedule?.end as? ProjectDate.Set)?.date
        BlockerInfo(
            taskId = id,
            title = task?.title?.value.orEmpty(),
            status = task?.status ?: ProjectTaskStatus.EMPTY,
            start = start,
            end = end,
            depth = depthMap[id] ?: 0,
        )
    }

    return BlockerChainResult(targetTaskId = taskId, blockers = blockers)
}

fun ProjectPlan.availableTasks(today: LocalDate): AvailableTasksResult {
    val tasksByIdMap = tasks().associateBy { it.id }
    val available = mutableListOf<AvailableTaskInfo>()

    for (task in tasks()) {
        val schedule = schedules()[TaskScheduleId(task.id.value)] ?: continue
        val start = (schedule.start as? ProjectDate.Set)?.date ?: continue
        val end = (schedule.end as? ProjectDate.Set)?.date ?: continue

        if (task.status == ProjectTaskStatus.DONE) continue
        if (start > today) continue

        val preds = dependencies().filter { it.successor == task.id }
        val allScheduledPredsDone = preds.all { dep ->
            val predSchedule = schedules()[TaskScheduleId(dep.predecessor.value)]
            val predScheduled = predSchedule != null
                    && predSchedule.start is ProjectDate.Set
                    && predSchedule.end is ProjectDate.Set
            if (!predScheduled) {
                true // unscheduled predecessors are ignored
            } else {
                val predTask = tasksByIdMap[dep.predecessor]
                predTask?.status == ProjectTaskStatus.DONE
            }
        }

        if (!allScheduledPredsDone) continue

        available.add(
            AvailableTaskInfo(
                taskId = task.id,
                title = task.title.value,
                status = task.status,
                start = start,
                end = end,
            )
        )
    }

    return AvailableTasksResult(tasks = available, asOfDate = today)
}

fun ProjectPlan.whatIf(
    taskId: TaskId,
    newStart: LocalDate,
    calendar: TimelineCalendar,
): WhatIfResult {
    val oldSchedules = mutableMapOf<TaskId, Pair<LocalDate, LocalDate>>()
    for ((schedId, schedule) in schedules()) {
        val start = (schedule.start as? ProjectDate.Set)?.date ?: continue
        val end = (schedule.end as? ProjectDate.Set)?.date ?: continue
        oldSchedules[TaskId(schedId.value)] = start to end
    }

    val sim = this.snapshot()
    val delta = sim.changeTaskStartDate(taskId, newStart, calendar)

    val tasksByIdMap = tasks().associateBy { it.id }
    val impacts = mutableListOf<TaskImpact>()

    for (updatedSchedule in delta.updatedSchedule) {
        val tid = TaskId(updatedSchedule.id.value)
        val oldDates = oldSchedules[tid] ?: continue
        val newStartDate = (updatedSchedule.start as ProjectDate.Set).date
        val newEndDate = (updatedSchedule.end as ProjectDate.Set).date
        val deltaStart = oldDates.first.daysUntil(newStartDate)
        val deltaEnd = oldDates.second.daysUntil(newEndDate)

        if (deltaStart == 0 && deltaEnd == 0 && tid != taskId) continue

        impacts.add(
            TaskImpact(
                taskId = tid,
                title = tasksByIdMap[tid]?.title?.value.orEmpty(),
                oldStart = oldDates.first,
                oldEnd = oldDates.second,
                newStart = newStartDate,
                newEnd = newEndDate,
                deltaStartDays = deltaStart,
                deltaEndDays = deltaEnd,
            )
        )
    }

    return WhatIfResult(movedTaskId = taskId, impacts = impacts)
}

fun ProjectPlan.whatIfEnd(
    taskId: TaskId,
    newEnd: LocalDate,
    calendar: TimelineCalendar,
): WhatIfResult {
    val oldSchedules = mutableMapOf<TaskId, Pair<LocalDate, LocalDate>>()
    for ((schedId, schedule) in schedules()) {
        val start = (schedule.start as? ProjectDate.Set)?.date ?: continue
        val end = (schedule.end as? ProjectDate.Set)?.date ?: continue
        oldSchedules[TaskId(schedId.value)] = start to end
    }

    val sim = this.snapshot()
    val delta = sim.changeTaskEndDate(taskId, newEnd, calendar)

    val tasksByIdMap = tasks().associateBy { it.id }
    val impacts = mutableListOf<TaskImpact>()

    for (updatedSchedule in delta.updatedSchedule) {
        val tid = TaskId(updatedSchedule.id.value)
        val oldDates = oldSchedules[tid] ?: continue
        val newStartDate = (updatedSchedule.start as ProjectDate.Set).date
        val newEndDate = (updatedSchedule.end as ProjectDate.Set).date
        val deltaStart = oldDates.first.daysUntil(newStartDate)
        val deltaEnd = oldDates.second.daysUntil(newEndDate)

        if (deltaStart == 0 && deltaEnd == 0 && tid != taskId) continue

        impacts.add(
            TaskImpact(
                taskId = tid,
                title = tasksByIdMap[tid]?.title?.value.orEmpty(),
                oldStart = oldDates.first,
                oldEnd = oldDates.second,
                newStart = newStartDate,
                newEnd = newEndDate,
                deltaStartDays = deltaStart,
                deltaEndDays = deltaEnd,
            )
        )
    }

    return WhatIfResult(movedTaskId = taskId, impacts = impacts)
}
