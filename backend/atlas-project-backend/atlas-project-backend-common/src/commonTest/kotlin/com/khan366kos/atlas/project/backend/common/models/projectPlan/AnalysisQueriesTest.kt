package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AnalysisQueriesTest {

    private val calendar = TimelineCalendar(
        workingWeekDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        ),
        weekendWeekDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    )

    private fun taskId(id: String) = TaskId(id)
    private fun schedId(id: String) = TaskScheduleId(id)

    private fun buildPlan(
        tasks: List<ProjectTask>,
        schedules: List<TaskSchedule>,
        dependencies: Set<TaskDependency> = emptySet(),
    ): ProjectPlan {
        val taskMap = tasks.associateBy { it.id }.toMutableMap()
        val schedMap = schedules.associateBy { it.id }.toMutableMap()
        return ProjectPlan(
            tasks = taskMap,
            schedules = schedMap,
            dependencies = dependencies.toMutableSet(),
        )
    }

    // --- blockerChain tests ---

    @Test
    fun blockerChain_noPredecessors() {
        val plan = buildPlan(
            tasks = listOf(ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A"))),
            schedules = listOf(
                TaskSchedule(
                    id = schedId("A"),
                    start = ProjectDate.Set(LocalDate(2025, 3, 3)),
                    end = ProjectDate.Set(LocalDate(2025, 3, 3)),
                )
            ),
        )

        val result = plan.blockerChain(taskId("A"))

        assertEquals(taskId("A"), result.targetTaskId)
        assertTrue(result.blockers.isEmpty())
    }

    @Test
    fun blockerChain_linearChain() {
        // A -> B -> C (FS). Query C. Should return [A(depth=2), B(depth=1)]
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B")),
                ProjectTask(id = taskId("C"), duration = Duration(1), title = Title("Task C")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
                TaskSchedule(id = schedId("C"), start = ProjectDate.Set(LocalDate(2025, 3, 5)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
                TaskDependency(predecessor = taskId("B"), successor = taskId("C"), type = DependencyType.FS),
            ),
        )

        val result = plan.blockerChain(taskId("C"))

        assertEquals(taskId("C"), result.targetTaskId)
        assertEquals(2, result.blockers.size)
        // Topological order: A first, then B
        assertEquals(taskId("A"), result.blockers[0].taskId)
        assertEquals(2, result.blockers[0].depth)
        assertEquals(taskId("B"), result.blockers[1].taskId)
        assertEquals(1, result.blockers[1].depth)
    }

    @Test
    fun blockerChain_diamond() {
        // A -> C, B -> C (both FS). Query C. Both are immediate predecessors (depth=1).
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B")),
                ProjectTask(id = taskId("C"), duration = Duration(1), title = Title("Task C")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("C"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("C"), type = DependencyType.FS),
                TaskDependency(predecessor = taskId("B"), successor = taskId("C"), type = DependencyType.FS),
            ),
        )

        val result = plan.blockerChain(taskId("C"))

        assertEquals(taskId("C"), result.targetTaskId)
        assertEquals(2, result.blockers.size)
        assertTrue(result.blockers.all { it.depth == 1 })
        val ids = result.blockers.map { it.taskId }.toSet()
        assertEquals(setOf(taskId("A"), taskId("B")), ids)
    }

    // --- availableTasks tests ---

    @Test
    fun availableTasks_allPredsDone() {
        val today = LocalDate(2025, 3, 10)
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A"), status = ProjectTaskStatus.DONE),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B"), status = ProjectTaskStatus.BACKLOG),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        val result = plan.availableTasks(today)

        assertEquals(1, result.tasks.size)
        assertEquals(taskId("B"), result.tasks[0].taskId)
        assertEquals(today, result.asOfDate)
    }

    @Test
    fun availableTasks_predNotDone() {
        val today = LocalDate(2025, 3, 10)
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A"), status = ProjectTaskStatus.IN_PROGRESS),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B"), status = ProjectTaskStatus.BACKLOG),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        val result = plan.availableTasks(today)

        // B should NOT be available because A is not DONE
        assertFalse(result.tasks.any { it.taskId == taskId("B") })
    }

    @Test
    fun availableTasks_futureStart() {
        val today = LocalDate(2025, 3, 10)
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A"), status = ProjectTaskStatus.BACKLOG),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 12)), end = ProjectDate.Set(LocalDate(2025, 3, 12))),
            ),
        )

        val result = plan.availableTasks(today)

        assertTrue(result.tasks.isEmpty())
    }

    @Test
    fun availableTasks_noPredsScheduled() {
        val today = LocalDate(2025, 3, 10)
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A"), status = ProjectTaskStatus.BACKLOG),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 10)), end = ProjectDate.Set(LocalDate(2025, 3, 10))),
            ),
        )

        val result = plan.availableTasks(today)

        assertEquals(1, result.tasks.size)
        assertEquals(taskId("A"), result.tasks[0].taskId)
    }

    // --- whatIf tests ---

    @Test
    fun whatIf_moveForward() {
        // A(1d) -> B(1d) FS. Move A forward by 2 days.
        // A: Mon Mar 3 -> Mar 3, B: Tue Mar 4 -> Mar 4
        // Move A to Wed Mar 5 -> A ends Mar 5, B starts Thu Mar 6
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        val result = plan.whatIf(taskId("A"), LocalDate(2025, 3, 5), calendar)

        assertEquals(taskId("A"), result.movedTaskId)
        assertTrue(result.impacts.size >= 2, "Both A and B should be impacted")

        val impactA = result.impacts.first { it.taskId == taskId("A") }
        assertEquals(LocalDate(2025, 3, 3), impactA.oldStart)
        assertEquals(LocalDate(2025, 3, 5), impactA.newStart)
        assertEquals(2, impactA.deltaStartDays)

        val impactB = result.impacts.first { it.taskId == taskId("B") }
        assertEquals(LocalDate(2025, 3, 4), impactB.oldStart)
        assertTrue(impactB.deltaStartDays > 0)
    }

    @Test
    fun whatIf_noSuccessors() {
        // Task A alone, no deps. Move A.
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
            ),
        )

        val result = plan.whatIf(taskId("A"), LocalDate(2025, 3, 5), calendar)

        assertEquals(1, result.impacts.size)
        assertEquals(taskId("A"), result.impacts[0].taskId)
        assertEquals(2, result.impacts[0].deltaStartDays)
        assertEquals(2, result.impacts[0].deltaEndDays)
    }

    @Test
    fun whatIf_originalUnchanged() {
        // After whatIf, verify original plan's schedules haven't changed.
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        plan.whatIf(taskId("A"), LocalDate(2025, 3, 5), calendar)

        // Original plan should be unchanged
        val schedA = plan.schedules()[schedId("A")]
        val schedB = plan.schedules()[schedId("B")]
        assertEquals(ProjectDate.Set(LocalDate(2025, 3, 3)), schedA?.start)
        assertEquals(ProjectDate.Set(LocalDate(2025, 3, 3)), schedA?.end)
        assertEquals(ProjectDate.Set(LocalDate(2025, 3, 4)), schedB?.start)
        assertEquals(ProjectDate.Set(LocalDate(2025, 3, 4)), schedB?.end)
    }

    // --- whatIfEnd tests ---

    @Test
    fun whatIfEnd_extendDuration_cascadesSuccessors() {
        // A(1d) -> B(1d) FS. Extend A end from Mar 3 to Mar 5 (duration 1->3).
        // A: Mon Mar 3 -> Mar 3, B: Tue Mar 4 -> Mar 4
        // After: A end = Mar 5 (Wed), B constrained start = addWorkingDays(Mar5, 2) = Mar 6 (Thu)
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        val result = plan.whatIfEnd(taskId("A"), LocalDate(2025, 3, 5), calendar)

        assertEquals(taskId("A"), result.movedTaskId)
        assertTrue(result.impacts.size >= 2, "Both A and B should be impacted")

        val impactA = result.impacts.first { it.taskId == taskId("A") }
        // A: start unchanged, end moved from Mar 3 to Mar 5
        assertEquals(LocalDate(2025, 3, 3), impactA.newStart)
        assertEquals(LocalDate(2025, 3, 5), impactA.newEnd)
        assertEquals(0, impactA.deltaStartDays)
        assertEquals(2, impactA.deltaEndDays)

        val impactB = result.impacts.first { it.taskId == taskId("B") }
        assertTrue(impactB.deltaStartDays > 0, "B should be pushed later")
    }

    @Test
    fun whatIfEnd_originalUnchanged() {
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1), title = Title("Task A")),
                ProjectTask(id = taskId("B"), duration = Duration(1), title = Title("Task B")),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        plan.whatIfEnd(taskId("A"), LocalDate(2025, 3, 5), calendar)

        val schedA = plan.schedules()[schedId("A")]
        assertEquals(ProjectDate.Set(LocalDate(2025, 3, 3)), schedA?.start)
        assertEquals(ProjectDate.Set(LocalDate(2025, 3, 3)), schedA?.end)
    }
}
