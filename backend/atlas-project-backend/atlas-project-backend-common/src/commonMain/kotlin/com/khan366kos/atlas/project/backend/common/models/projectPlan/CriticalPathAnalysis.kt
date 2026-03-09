package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.LocalDate

data class CpmTaskResult(
    val taskId: TaskId,
    val es: LocalDate,
    val ef: LocalDate,
    val ls: LocalDate,
    val lf: LocalDate,
    val slack: Int,
    val isCritical: Boolean,
)

data class CriticalPathResult(
    val tasks: Map<TaskId, CpmTaskResult>,
    val criticalTaskIds: Set<TaskId>,
    val projectEnd: LocalDate,
)

class CriticalPathAnalysis(
    private val plan: ProjectPlan,
    private val calendar: TimelineCalendar,
) {
    fun compute(): CriticalPathResult {
        val schedules = plan.schedules()
        val dependencies = plan.dependencies()

        // 1. Filter scheduled tasks (both start and end set)
        val scheduledIds = schedules.keys
            .filter { schedules[it]?.start is ProjectDate.Set && schedules[it]?.end is ProjectDate.Set }
            .map { TaskId(it.value) }
            .toSet()

        if (scheduledIds.isEmpty()) {
            error("No scheduled tasks for CPM analysis")
        }

        // 2. Topological sort
        val sortedIds = topologicalSort(scheduledIds, dependencies)

        // Duration map: actual duration from schedule
        val durationMap = mutableMapOf<TaskId, Duration>()
        for (id in sortedIds) {
            val sched = schedules[TaskScheduleId(id.value)]!!
            val start = (sched.start as ProjectDate.Set).date
            val end = (sched.end as ProjectDate.Set).date
            durationMap[id] = calendar.workingDaysBetween(start, end)
        }

        // Relevant deps (both ends scheduled)
        val relevantDeps = dependencies.filter { it.predecessor in scheduledIds && it.successor in scheduledIds }
        val incomingDeps = relevantDeps.groupBy { it.successor }
        val outgoingDeps = relevantDeps.groupBy { it.predecessor }

        // 3. Forward pass
        val esMap = mutableMapOf<TaskId, LocalDate>()
        val efMap = mutableMapOf<TaskId, LocalDate>()

        for (id in sortedIds) {
            val preds = incomingDeps[id]
            val duration = durationMap[id]!!

            if (preds.isNullOrEmpty()) {
                // Root task: ES = actual start from schedule
                val sched = schedules[TaskScheduleId(id.value)]!!
                esMap[id] = (sched.start as ProjectDate.Set).date
                efMap[id] = (sched.end as ProjectDate.Set).date
            } else {
                // ES = max constrained start from all predecessors
                val es = preds.mapNotNull { dep ->
                    val predSched = schedules[TaskScheduleId(dep.predecessor.value)]
                        ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                        ?: return@mapNotNull null
                    // Use ES/EF of predecessor (already computed) for constraint calculation
                    val predES = esMap[dep.predecessor] ?: return@mapNotNull null
                    val predEF = efMap[dep.predecessor] ?: return@mapNotNull null
                    val predAsSchedule = com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule(
                        id = TaskScheduleId(dep.predecessor.value),
                        start = ProjectDate.Set(predES),
                        end = ProjectDate.Set(predEF),
                    )
                    calculateConstrainedStart(dep, predAsSchedule, duration, calendar)
                }.maxOrNull() ?: run {
                    // Fallback to actual schedule if no constraints resolved
                    (schedules[TaskScheduleId(id.value)]!!.start as ProjectDate.Set).date
                }

                esMap[id] = es
                efMap[id] = calendar.addWorkingDays(es, duration)
            }
        }

        // 4. Project end = max EF
        val projectEnd = efMap.values.max()

        // 5. Backward pass
        val lfMap = mutableMapOf<TaskId, LocalDate>()
        val lsMap = mutableMapOf<TaskId, LocalDate>()

        for (id in sortedIds.reversed()) {
            val succs = outgoingDeps[id]
            val duration = durationMap[id]!!

            // Start with projectEnd as upper bound for all tasks
            val lfFromSuccessors = if (!succs.isNullOrEmpty()) {
                succs.mapNotNull { dep ->
                    val succLS = lsMap[dep.successor] ?: return@mapNotNull null
                    val succLF = lfMap[dep.successor] ?: return@mapNotNull null
                    calculateConstrainedLF(dep, succLS, succLF, duration, calendar)
                }.minOrNull() ?: projectEnd
            } else {
                projectEnd
            }

            // LF can never exceed projectEnd
            val lf = minOf(lfFromSuccessors, projectEnd)
            lfMap[id] = lf
            lsMap[id] = calendar.subtractWorkingDays(lf, duration)
        }

        // 6. Compute slack and criticality
        val results = mutableMapOf<TaskId, CpmTaskResult>()
        val criticalIds = mutableSetOf<TaskId>()

        for (id in sortedIds) {
            val es = esMap[id]!!
            val ef = efMap[id]!!
            val ls = lsMap[id]!!
            val lf = lfMap[id]!!
            // workingDaysBetween counts inclusive, so Mon→Mon = 1. Slack = that - 1.
            val slack = if (es <= ls) calendar.workingDaysBetween(es, ls).asInt() - 1 else 0
            val isCritical = slack == 0

            results[id] = CpmTaskResult(
                taskId = id,
                es = es, ef = ef,
                ls = ls, lf = lf,
                slack = slack,
                isCritical = isCritical,
            )

            if (isCritical) criticalIds.add(id)
        }

        return CriticalPathResult(
            tasks = results,
            criticalTaskIds = criticalIds,
            projectEnd = projectEnd,
        )
    }
}
