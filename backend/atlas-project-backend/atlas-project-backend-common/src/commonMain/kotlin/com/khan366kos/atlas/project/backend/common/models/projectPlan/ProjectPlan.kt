package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.ScheduleDelta
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.LocalDate

data class ProjectPlan(
    val id: ProjectPlanId = ProjectPlanId.NONE,
    val projectId: ProjectId = ProjectId.NONE,
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

    fun changeDependencyType(
        predecessorId: TaskId,
        successorId: TaskId,
        newType: DependencyType,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        val dep = dependencies.find { it.predecessor == predecessorId && it.successor == successorId }
            ?: error("Dependency not found: $predecessorId -> $successorId")

        // Replace with new type and default lag for the new type
        dependencies.remove(dep)
        val newLag = if (newType == DependencyType.SF) 1 else 0
        val newDep = dep.copy(type = newType, lag = Duration(newLag))
        dependencies.add(newDep)

        // Recalculate successor schedule
        val updatedSchedules = mutableListOf<TaskSchedule>()
        val successorTask = tasks[successorId] ?: return ScheduleDelta(emptyList())
        val successorSchedule = schedules[TaskScheduleId(successorId.value)]
            ?: return ScheduleDelta(emptyList())
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

    fun removeDependency(
        predecessorId: TaskId,
        successorId: TaskId,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        dependencies.removeIf { it.predecessor == predecessorId && it.successor == successorId }

        val updatedSchedules = mutableListOf<TaskSchedule>()
        val remainingPreds = dependencies.filter { it.successor == successorId }

        if (remainingPreds.isNotEmpty()) {
            val successorSchedule = schedules[TaskScheduleId(successorId.value)]
                ?: return ScheduleDelta(emptyList())
            val successorTask = tasks[successorId] ?: return ScheduleDelta(emptyList())
            val successorDuration = actualDuration(successorSchedule, calendar) ?: successorTask.duration

            val constrainedStart = remainingPreds.mapNotNull { pd ->
                val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                    ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                    ?: return@mapNotNull null
                calculateConstrainedStart(pd, predSched, successorDuration, calendar)
            }.maxOrNull()

            if (constrainedStart != null) {
                val constrainedEnd = calendar.addWorkingDays(constrainedStart, successorDuration)
                val updatedSched = TaskSchedule(
                    id = TaskScheduleId(successorId.value),
                    start = ProjectDate.Set(constrainedStart),
                    end = ProjectDate.Set(constrainedEnd),
                )
                schedules[updatedSched.id] = updatedSched
                updatedSchedules.add(updatedSched)
            }
        }

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

                // Only move the successor if the constraint pushes it later
                val currentStart = (successorSchedule?.start as? ProjectDate.Set)?.date
                if (currentStart != null && constrainedStart <= currentStart) continue

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

    fun validateNoCycles(): Boolean {
        return try {
            topologicalSort(tasks.keys, dependencies)
            true
        } catch (_: IllegalStateException) {
            false
        }
    }

    fun snapshot(): ProjectPlan = ProjectPlan(
        id = id,
        projectId = projectId,
        tasks = tasks.toMutableMap(),
        schedules = schedules.mapValues { (_, v) -> v.copy() }.toMutableMap(),
        dependencies = dependencies.toMutableSet(),
    )

    private fun calculateLag(
        predecessorId: TaskId,
        successorId: TaskId,
        type: DependencyType,
        calendar: TimelineCalendar,
    ): Int {
        return when (type) {
            // FS, SS, FF: lag=0 at creation, successor snaps to constraint
            DependencyType.FS, DependencyType.SS, DependencyType.FF -> 0
            // SF: lag computed from current positions, minimum 1
            DependencyType.SF -> {
                val predSchedule = schedules[TaskScheduleId(predecessorId.value)]
                    ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                    ?: return 1
                val succSchedule = schedules[TaskScheduleId(successorId.value)]
                    ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                    ?: return 1
                val predStart = (predSchedule.start as ProjectDate.Set).date
                val succEnd = (succSchedule.end as ProjectDate.Set).date
                if (succEnd >= predStart) {
                    maxOf(1, calendar.workingDaysBetween(predStart, succEnd).asInt())
                } else {
                    1
                }
            }
        }
    }

    private fun clampStartByIncomingDeps(
        taskId: TaskId,
        requestedStart: LocalDate,
        taskDuration: Duration,
        calendar: TimelineCalendar,
    ): LocalDate {
        val incomingDeps = dependencies.filter { it.successor == taskId }
        if (incomingDeps.isEmpty()) return requestedStart

        val minAllowedStart = incomingDeps.mapNotNull { dep ->
            val predSched = schedules[TaskScheduleId(dep.predecessor.value)]
                ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                ?: return@mapNotNull null
            calculateConstrainedStart(dep, predSched, taskDuration, calendar)
        }.maxOrNull() ?: return requestedStart

        return maxOf(requestedStart, minAllowedStart)
    }

    private fun clampEndByIncomingDeps(
        taskId: TaskId,
        requestedEnd: LocalDate,
        calendar: TimelineCalendar,
    ): LocalDate {
        val incomingDeps = dependencies.filter { it.successor == taskId }
        if (incomingDeps.isEmpty()) return requestedEnd

        val minAllowedEnd = incomingDeps.mapNotNull { dep ->
            val predSched = schedules[TaskScheduleId(dep.predecessor.value)]
                ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                ?: return@mapNotNull null
            calculateConstrainedEnd(dep, predSched, calendar)
        }.maxOrNull() ?: return requestedEnd

        return maxOf(requestedEnd, minAllowedEnd)
    }

    private fun calculateConstrainedEnd(
        dep: TaskDependency,
        predSchedule: TaskSchedule,
        calendar: TimelineCalendar,
    ): LocalDate? {
        val predStart = (predSchedule.start as ProjectDate.Set).date
        val predEnd = (predSchedule.end as ProjectDate.Set).date
        val lag = dep.lag.asInt()
        return when (dep.type) {
            // FS and SS constrain the start, not the end — no end restriction for resize
            DependencyType.FS, DependencyType.SS -> null
            DependencyType.FF -> calendar.addWorkingDays(predEnd, Duration(lag))
            DependencyType.SF -> calendar.addWorkingDays(predStart, Duration(lag))
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

        // Step 1 — clamp newStart against incoming dependency constraints
        val task = tasks[taskId] ?: error("Unknown task $taskId")
        val clampedStart = clampStartByIncomingDeps(taskId, newStart, task.duration, calendar)

        // Step 2 — move the target task
        val newEnd = calendar.addWorkingDays(clampedStart, task.duration)
        val newSchedule = TaskSchedule(
            id = TaskScheduleId(taskId.value),
            start = ProjectDate.Set(clampedStart),
            end = ProjectDate.Set(newEnd),
        )
        schedules[newSchedule.id] = newSchedule
        updatedSchedules.add(newSchedule)

        // Step 3 — BFS cascade through dependency graph
        updatedSchedules.addAll(cascadeBfs(taskId, calendar))

        return ScheduleDelta(updatedSchedules)
    }

    fun resizeTaskFromStart(
        taskId: TaskId,
        newStart: LocalDate,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        val updatedSchedules = mutableListOf<TaskSchedule>()

        val task = tasks[taskId] ?: error("Unknown task $taskId")
        val currentSchedule = schedules[TaskScheduleId(taskId.value)]
            ?: error("No schedule for task $taskId")
        val currentEnd = (currentSchedule.end as ProjectDate.Set).date

        // Clamp start by FS/SS incoming deps only (FF/SF constrain end, which is preserved)
        val startConstrainingDeps = dependencies.filter {
            it.successor == taskId && (it.type == DependencyType.FS || it.type == DependencyType.SS)
        }
        val minAllowedStart = startConstrainingDeps.mapNotNull { dep ->
            val predSched = schedules[TaskScheduleId(dep.predecessor.value)]
                ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                ?: return@mapNotNull null
            calculateConstrainedStart(dep, predSched, task.duration, calendar)
        }.maxOrNull()

        var finalStart = if (minAllowedStart != null) maxOf(newStart, minAllowedStart) else newStart

        // Ensure minimum 1 working day duration
        val maxAllowedStart = calendar.subtractWorkingDays(currentEnd, Duration(1))
        if (finalStart > maxAllowedStart) finalStart = maxAllowedStart

        // Update duration and schedule
        val newDuration = calendar.workingDaysBetween(finalStart, currentEnd)
        tasks[taskId] = task.copy(duration = newDuration)

        val newSchedule = TaskSchedule(
            id = TaskScheduleId(taskId.value),
            start = ProjectDate.Set(finalStart),
            end = ProjectDate.Set(currentEnd),
        )
        schedules[newSchedule.id] = newSchedule
        updatedSchedules.add(newSchedule)

        updatedSchedules.addAll(cascadeBfs(taskId, calendar))
        return ScheduleDelta(updatedSchedules)
    }

    fun changeTaskEndDate(
        taskId: TaskId,
        newEnd: LocalDate,
        calendar: TimelineCalendar
    ): ScheduleDelta {
        val updatedSchedules = mutableListOf<TaskSchedule>()

        // Step 1 — clamp newEnd against incoming dependency constraints (FF, SF)
        val task = tasks[taskId] ?: error("Unknown task $taskId")
        val currentSchedule = schedules[TaskScheduleId(taskId.value)]
            ?: error("No schedule for task $taskId")
        val currentStart = (currentSchedule.start as ProjectDate.Set).date
        val clampedEnd = clampEndByIncomingDeps(taskId, newEnd, calendar)

        // Step 2 — keep start date unchanged, calculate new duration
        val newDuration = calendar.workingDaysBetween(currentStart, clampedEnd)

        // Update task duration
        val updatedTask = task.copy(duration = newDuration)
        tasks[taskId] = updatedTask

        // Update schedule with same start, new end
        val newSchedule = TaskSchedule(
            id = TaskScheduleId(taskId.value),
            start = ProjectDate.Set(currentStart),
            end = ProjectDate.Set(clampedEnd),
        )
        schedules[newSchedule.id] = newSchedule
        updatedSchedules.add(newSchedule)

        // Step 3 — BFS cascade through dependency graph
        updatedSchedules.addAll(cascadeBfs(taskId, calendar))

        return ScheduleDelta(updatedSchedules)
    }

    private fun actualDuration(schedule: TaskSchedule, calendar: TimelineCalendar): Duration? {
        val start = (schedule.start as? ProjectDate.Set)?.date ?: return null
        val end = (schedule.end as? ProjectDate.Set)?.date ?: return null
        return calendar.workingDaysBetween(start, end)
    }

    fun recalculateAll(calendar: TimelineCalendar): ScheduleDelta {
        val scheduledIds = schedules.keys
            .filter { schedules[it]?.start is ProjectDate.Set && schedules[it]?.end is ProjectDate.Set }
            .map { TaskId(it.value) }
            .toSet()

        val sortedIds = topologicalSort(scheduledIds, dependencies)
        val updatedSchedules = mutableListOf<TaskSchedule>()

        for (currentId in sortedIds) {
            val allPreds = dependencies.filter { it.successor == currentId }
            if (allPreds.isEmpty()) continue

            val currentSchedule = schedules[TaskScheduleId(currentId.value)] ?: continue
            val duration = actualDuration(currentSchedule, calendar)
                ?: tasks[currentId]?.duration ?: continue

            val constrainedStart = allPreds.mapNotNull { pd ->
                val predSched = schedules[TaskScheduleId(pd.predecessor.value)]
                    ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                    ?: return@mapNotNull null
                calculateConstrainedStart(pd, predSched, duration, calendar)
            }.maxOrNull() ?: continue

            val constrainedEnd = calendar.addWorkingDays(constrainedStart, duration)
            val sched = TaskSchedule(
                id = TaskScheduleId(currentId.value),
                start = ProjectDate.Set(constrainedStart),
                end = ProjectDate.Set(constrainedEnd),
            )
            schedules[sched.id] = sched
            updatedSchedules.add(sched)
        }

        return ScheduleDelta(updatedSchedules)
    }

    private fun calculateConstrainedStart(
        dep: TaskDependency,
        predSchedule: TaskSchedule,
        successorDuration: Duration,
        calendar: TimelineCalendar,
    ): LocalDate = com.khan366kos.atlas.project.backend.common.models.projectPlan.calculateConstrainedStart(
        dep, predSchedule, successorDuration, calendar
    )
}