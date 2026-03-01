package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.ScheduleDelta
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.LocalDate

data class ProjectPlan(
    val id: ProjectPlanId = ProjectPlanId.NONE,
    private val tasks: MutableMap<TaskId, ProjectTask> = mutableMapOf(),
    private val schedules: MutableMap<TaskScheduleId, TaskSchedule>,
    private val dependencies: MutableSet<TaskDependency> = mutableSetOf(),
) {
    fun tasks() = tasks.values.toList()
    fun schedules() = schedules
    fun dependencies() = dependencies

    fun addDependency(
        predecessorId: TaskId,
        successorId: TaskId,
        type: DependencyType,
        lagDays: Int?,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        // Check for circular dependency
        if (wouldCreateCycle(predecessorId, successorId)) {
            error("Adding this dependency would create a circular dependency")
        }

        // Calculate lag if not provided
        val lag = lagDays ?: calculateLag(predecessorId, successorId, type, calendar)

        // Add the dependency
        val dependency = TaskDependency(
            predecessor = predecessorId,
            successor = successorId,
            type = type,
            lag = Duration(lag)
        )
        dependencies.add(dependency)

        // Recalculate the successor task's schedule based on all its predecessors
        val updatedSchedules = mutableListOf<TaskSchedule>()
        val successorTask = tasks[successorId] ?: return ScheduleDelta(emptyList())
        val successorSchedule = schedules[TaskScheduleId(successorId.value)]
            ?: return ScheduleDelta(emptyList())

        val allPreds = dependencies.filter { it.successor == successorId }
        val constrainedStart = allPreds.mapNotNull { pd ->
            val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                ?: return@mapNotNull null
            calculateConstrainedStart(pd, predSched, successorTask.duration, calendar)
        }.maxOrNull() ?: (successorSchedule.start as? ProjectDate.Set)?.date ?: return ScheduleDelta(emptyList())

        val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorTask.duration)
        val updatedSched = TaskSchedule(
            id = TaskScheduleId(successorId.value),
            start = ProjectDate.Set(constrainedStart),
            end = ProjectDate.Set(constrainedEnd),
        )
        schedules[updatedSched.id] = updatedSched
        updatedSchedules.add(updatedSched)

        // BFS cascade through dependency graph
        val queue = ArrayDeque<TaskId>()
        val visited = mutableSetOf(successorId)
        queue.add(successorId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()

            val outgoing = dependencies.filter { it.predecessor == currentId }
            for (dep in outgoing) {
                val nextSuccessorId = dep.successor
                val nextSuccessorTask = tasks[nextSuccessorId] ?: continue

                val allPredsOfSuccessor = dependencies.filter { it.successor == nextSuccessorId }
                val nextConstrainedStart = allPredsOfSuccessor.mapNotNull { pd ->
                    val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                        ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                        ?: return@mapNotNull null
                    calculateConstrainedStart(pd, predSched, nextSuccessorTask.duration, calendar)
                }.maxOrNull() ?: continue

                val nextConstrainedEnd = calendar.addWorkingDays(nextConstrainedStart, nextSuccessorTask.duration)
                val nextUpdatedSched = TaskSchedule(
                    id = TaskScheduleId(nextSuccessorId.value),
                    start = ProjectDate.Set(nextConstrainedStart),
                    end = ProjectDate.Set(nextConstrainedEnd),
                )
                schedules[nextUpdatedSched.id] = nextUpdatedSched
                updatedSchedules.add(nextUpdatedSched)

                if (nextSuccessorId !in visited) {
                    visited.add(nextSuccessorId)
                    queue.add(nextSuccessorId)
                }
            }
        }

        return ScheduleDelta(updatedSchedules)
    }

    private fun wouldCreateCycle(predecessorId: TaskId, successorId: TaskId): Boolean {
        // Check if successor is already an ancestor of predecessor
        val visited = mutableSetOf<TaskId>()
        val queue = ArrayDeque<TaskId>()
        queue.add(predecessorId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            if (currentId == successorId) return true
            if (currentId in visited) continue
            visited.add(currentId)

            // Find all tasks that currentId depends on (predecessors)
            val preds = dependencies.filter { it.successor == currentId }.map { it.predecessor }
            queue.addAll(preds)
        }

        return false
    }

    private fun calculateLag(
        predecessorId: TaskId,
        successorId: TaskId,
        type: DependencyType,
        calendar: TimelineCalendar
    ): Int {
        val predSchedule = schedules[TaskScheduleId(predecessorId.value)]
            ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
            ?: return 0
        val succSchedule = schedules[TaskScheduleId(successorId.value)]
            ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
            ?: return 0

        val predStart = (predSchedule.start as ProjectDate.Set).date
        val predEnd = (predSchedule.end as ProjectDate.Set).date
        val succStart = (succSchedule.start as ProjectDate.Set).date
        val succEnd = (succSchedule.end as ProjectDate.Set).date

        return when (type) {
            DependencyType.FS -> {
                // Для FS: задача должна начаться на lag+1 рабочих дней после окончания предшественника
                // Если задача уже начинается после predEnd, рассчитываем lag
                // Если задача начинается до или в predEnd, lag = 0 (задача будет перенесена)
                if (succStart <= predEnd) {
                    0 // Задача будет перенесена на следующий рабочий день после predEnd
                } else {
                    val daysBetween = calendar.workingDaysBetween(predEnd, succStart).asInt()
                    (daysBetween - 1).coerceAtLeast(0)
                }
            }
            DependencyType.SS -> {
                // Lag = working days between predStart and succStart
                calendar.workingDaysBetween(predStart, succStart).asInt()
            }
            DependencyType.FF -> {
                // Lag = working days between predEnd and succEnd
                calendar.workingDaysBetween(predEnd, succEnd).asInt()
            }
            DependencyType.SF -> {
                // Lag = working days between predStart and succEnd
                calendar.workingDaysBetween(predStart, succEnd).asInt()
            }
        }
    }

    fun changeTaskStartDate(
        taskId: TaskId,
        newStart: LocalDate,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        val updatedSchedules = mutableListOf<TaskSchedule>()

        // Step 1 — move the target task
        val task = tasks[taskId] ?: error("Unknown task $taskId")
        val newEnd = calendar.addWorkingDays(newStart, task.duration)
        val newSchedule = TaskSchedule(
            id = TaskScheduleId(taskId.value),
            start = ProjectDate.Set(newStart),
            end = ProjectDate.Set(newEnd),
        )
        schedules[newSchedule.id] = newSchedule
        updatedSchedules.add(newSchedule)

        // Step 2 — BFS cascade through dependency graph
        val queue = ArrayDeque<TaskId>()
        val visited = mutableSetOf(taskId)
        queue.add(taskId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()

            val outgoing = dependencies.filter { it.predecessor == currentId }
            for (dep in outgoing) {
                val successorId = dep.successor
                val successorTask = tasks[successorId] ?: continue

                val allPredsOfSuccessor = dependencies.filter { it.successor == successorId }
                val constrainedStart = allPredsOfSuccessor.mapNotNull { pd ->
                    val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                        ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                        ?: return@mapNotNull null
                    calculateConstrainedStart(pd, predSched, successorTask.duration, calendar)
                }.maxOrNull() ?: continue

                val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorTask.duration)
                val updatedSched = TaskSchedule(
                    id = TaskScheduleId(successorId.value),
                    start = ProjectDate.Set(constrainedStart),
                    end = ProjectDate.Set(constrainedEnd),
                )
                schedules[updatedSched.id] = updatedSched
                updatedSchedules.add(updatedSched)

                if (successorId !in visited) {
                    visited.add(successorId)
                    queue.add(successorId)
                }
            }
        }

        return ScheduleDelta(updatedSchedules)
    }

    fun changeTaskEndDate(
        taskId: TaskId,
        newEnd: LocalDate,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        val updatedSchedules = mutableListOf<TaskSchedule>()

        // Step 1 — keep start date unchanged, calculate new duration
        val task = tasks[taskId] ?: error("Unknown task $taskId")
        val currentSchedule = schedules[TaskScheduleId(taskId.value)]
            ?: error("No schedule for task $taskId")
        val currentStart = (currentSchedule.start as ProjectDate.Set).date
        val newDuration = calendar.workingDaysBetween(currentStart, newEnd)
        
        // Update task duration
        val updatedTask = task.copy(duration = newDuration)
        tasks[taskId] = updatedTask
        
        // Update schedule with same start, new end
        val newSchedule = TaskSchedule(
            id = TaskScheduleId(taskId.value),
            start = ProjectDate.Set(currentStart),
            end = ProjectDate.Set(newEnd),
        )
        schedules[newSchedule.id] = newSchedule
        updatedSchedules.add(newSchedule)

        // Step 2 — BFS cascade through dependency graph
        val queue = ArrayDeque<TaskId>()
        val visited = mutableSetOf(taskId)
        queue.add(taskId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()

            val outgoing = dependencies.filter { it.predecessor == currentId }
            for (dep in outgoing) {
                val successorId = dep.successor
                val successorTask = tasks[successorId] ?: continue

                val allPredsOfSuccessor = dependencies.filter { it.successor == successorId }
                val constrainedStart = allPredsOfSuccessor.mapNotNull { pd ->
                    val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                        ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                        ?: return@mapNotNull null
                    calculateConstrainedStart(pd, predSched, successorTask.duration, calendar)
                }.maxOrNull() ?: continue

                val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorTask.duration)
                val updatedSched = TaskSchedule(
                    id = TaskScheduleId(successorId.value),
                    start = ProjectDate.Set(constrainedStart),
                    end = ProjectDate.Set(constrainedEnd),
                )
                schedules[updatedSched.id] = updatedSched
                updatedSchedules.add(updatedSched)

                if (successorId !in visited) {
                    visited.add(successorId)
                    queue.add(successorId)
                }
            }
        }

        return ScheduleDelta(updatedSchedules)
    }

    private fun calculateConstrainedStart(
        dep: TaskDependency,
        predSchedule: TaskSchedule,
        successorDuration: Duration,
        calendar: TimelineCalendar,
    ): LocalDate {
        val predStart = (predSchedule.start as ProjectDate.Set).date
        val predEnd = (predSchedule.end as ProjectDate.Set).date
        val lag = dep.lag.asInt()
        return when (dep.type) {
            DependencyType.FS -> calendar.addWorkingDays(predEnd, Duration(lag + 1))
            DependencyType.SS -> calendar.addWorkingDays(predStart, Duration(lag))
            DependencyType.FF -> {
                val constrainedEnd = calendar.addWorkingDays(predEnd, Duration(lag))
                calendar.subtractWorkingDays(constrainedEnd, successorDuration)
            }
            DependencyType.SF -> {
                val constrainedEnd = calendar.addWorkingDays(predStart, Duration(lag))
                calendar.subtractWorkingDays(constrainedEnd, successorDuration)
            }
        }
    }
}