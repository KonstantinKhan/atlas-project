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

        // Compute actual duration from schedule to avoid stale durationDays in project_tasks
        val successorDuration = actualDuration(successorSchedule, calendar) ?: successorTask.duration

        val allPreds = dependencies.filter { it.successor == successorId }
        val constrainedStart = allPreds.mapNotNull { pd ->
            val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                ?: return@mapNotNull null
            calculateConstrainedStart(pd, predSched, successorDuration, calendar)
        }.maxOrNull() ?: (successorSchedule.start as? ProjectDate.Set)?.date ?: return ScheduleDelta(emptyList())

        val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorDuration)
        val updatedSched = TaskSchedule(
            id = TaskScheduleId(successorId.value),
            start = ProjectDate.Set(constrainedStart),
            end = ProjectDate.Set(constrainedEnd),
        )
        schedules[updatedSched.id] = updatedSched
        updatedSchedules.add(updatedSched)

        updatedSchedules.addAll(cascadeBfs(successorId, calendar))

        return ScheduleDelta(updatedSchedules)
    }

    private fun cascadeBfs(
        seedTaskId: TaskId,
        calendar: TimelineCalendar
    ): List<TaskSchedule> {
        val updatedSchedules = mutableListOf<TaskSchedule>()
        val queue = ArrayDeque<TaskId>()
        val visited = mutableSetOf(seedTaskId)
        queue.add(seedTaskId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            val outgoing = dependencies.filter { it.predecessor == currentId }
            for (dep in outgoing) {
                val successorId = dep.successor
                val successorTask = tasks[successorId] ?: continue
                val successorSchedule = schedules[TaskScheduleId(successorId.value)]
                val successorDuration = successorSchedule?.let { actualDuration(it, calendar) } ?: successorTask.duration
                val allPreds = dependencies.filter { it.successor == successorId }
                val constrainedStart = allPreds.mapNotNull { pd ->
                    val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                        ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                        ?: return@mapNotNull null
                    calculateConstrainedStart(pd, predSched, successorDuration, calendar)
                }.maxOrNull() ?: continue

                val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorDuration)
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
        return updatedSchedules
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
        calendar: TimelineCalendar,
        allowNegativeLag: Boolean = false,
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
                // lag = 0: встык (predEnd=Mon, succStart=Tue)
                // lag > 0: зазор в рабочих днях
                // lag < 0: опережение (succStart раньше predEnd) — только если allowNegativeLag
                if (succStart >= predEnd) {
                    calendar.workingDaysBetween(predEnd, succStart).asInt() - 2
                } else if (allowNegativeLag) {
                    -calendar.workingDaysBetween(succStart, predEnd).asInt()
                } else {
                    0 // overlap при создании зависимости → задача будет сдвинута на след. раб. день
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

    fun planFromEnd(
        taskId: TaskId,
        newEnd: LocalDate,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        val task = tasks[taskId] ?: error("Unknown task $taskId")
        val newStart = calendar.subtractWorkingDays(newEnd, task.duration)
        val newSchedule = TaskSchedule(
            id = TaskScheduleId(taskId.value),
            start = ProjectDate.Set(newStart),
            end = ProjectDate.Set(newEnd),
        )
        schedules[newSchedule.id] = newSchedule
        val updated = mutableListOf(newSchedule)
        updated.addAll(cascadeBfs(taskId, calendar))
        return ScheduleDelta(updated)
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
        updatedSchedules.addAll(cascadeBfs(taskId, calendar))

        // Step 3 — recalculate lag for incoming dependencies (user explicitly moved this task)
        val incomingDeps = dependencies.filter { it.successor == taskId }
        val updatedDeps = incomingDeps.mapNotNull { dep ->
            val newLag = calculateLag(dep.predecessor, taskId, dep.type, calendar, allowNegativeLag = true)
            if (newLag == dep.lag.asInt()) null
            else dep.copy(lag = Duration(newLag))
        }
        updatedDeps.forEach { updated ->
            dependencies.removeIf { it.predecessor == updated.predecessor && it.successor == updated.successor }
            dependencies.add(updated)
        }

        return ScheduleDelta(updatedSchedules, updatedDeps)
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
        updatedSchedules.addAll(cascadeBfs(taskId, calendar))

        return ScheduleDelta(updatedSchedules)
    }

    private fun actualDuration(schedule: TaskSchedule, calendar: TimelineCalendar): Duration? {
        val start = (schedule.start as? ProjectDate.Set)?.date ?: return null
        val end = (schedule.end as? ProjectDate.Set)?.date ?: return null
        return calendar.workingDaysBetween(start, end)
    }

    fun recalculateAll(calendar: TimelineCalendar): ScheduleDelta {
        val tasksWithPredecessors = dependencies.map { it.successor }.toSet()
        val rootIds = tasks.keys.filter { it !in tasksWithPredecessors }

        val queue = ArrayDeque<TaskId>()
        val visited = mutableSetOf<TaskId>()
        rootIds.forEach { queue.add(it); visited.add(it) }

        val updatedSchedules = mutableListOf<TaskSchedule>()
        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            for (dep in dependencies.filter { it.predecessor == currentId }) {
                val successorId = dep.successor
                if (successorId in visited) continue
                val successorSchedule = schedules[TaskScheduleId(successorId.value)] ?: continue
                val successorDuration = actualDuration(successorSchedule, calendar)
                    ?: tasks[successorId]?.duration ?: continue
                val allPreds = dependencies.filter { it.successor == successorId }
                val constrainedStart = allPreds.mapNotNull { pd ->
                    val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                        ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                        ?: return@mapNotNull null
                    calculateConstrainedStart(pd, predSched, successorDuration, calendar)
                }.maxOrNull() ?: continue
                val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorDuration)
                val sched = TaskSchedule(
                    id = TaskScheduleId(successorId.value),
                    start = ProjectDate.Set(constrainedStart),
                    end = ProjectDate.Set(constrainedEnd),
                )
                schedules[sched.id] = sched
                updatedSchedules.add(sched)
                visited.add(successorId)
                queue.add(successorId)
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
            DependencyType.FS -> {
                // lag=0 → встык (next working day), lag=1 → 1 day gap, lag=-1 → same day as predEnd, lag=-2 → 1 day before predEnd
                val n = lag + 2
                if (n > 0) calendar.addWorkingDays(predEnd, Duration(n))
                else calendar.subtractWorkingDays(predEnd, Duration(-lag))
            }
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