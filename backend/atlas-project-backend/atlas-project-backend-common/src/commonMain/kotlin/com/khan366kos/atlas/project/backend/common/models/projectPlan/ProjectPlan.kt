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