package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CriticalPathAnalysisTest {

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

    @Test
    fun singleTask_isCritical() {
        // Single task A: Mon Mar 3 -> Wed Mar 5, duration 3
        val plan = buildPlan(
            tasks = listOf(ProjectTask(id = taskId("A"), duration = Duration(3))),
            schedules = listOf(
                TaskSchedule(
                    id = schedId("A"),
                    start = ProjectDate.Set(LocalDate(2025, 3, 3)),
                    end = ProjectDate.Set(LocalDate(2025, 3, 5)),
                )
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        assertEquals(1, result.tasks.size)
        val a = result.tasks[taskId("A")]!!
        assertEquals(LocalDate(2025, 3, 3), a.es)
        assertEquals(LocalDate(2025, 3, 5), a.ef)
        assertEquals(LocalDate(2025, 3, 3), a.ls)
        assertEquals(LocalDate(2025, 3, 5), a.lf)
        assertEquals(0, a.slack)
        assertTrue(a.isCritical)
        assertTrue(taskId("A") in result.criticalTaskIds)
        assertEquals(LocalDate(2025, 3, 5), result.projectEnd)
    }

    @Test
    fun linearChain_FS_allCritical() {
        // A(1d) -> B(1d) -> C(1d), all FS lag=0
        // A: Mon Mar 3
        // FS lag=0: B starts addWorkingDays(Mar3, 2) = Mar 4 (Tue)
        // FS lag=0: C starts addWorkingDays(Mar4, 2) = Mar 5 (Wed)
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
                ProjectTask(id = taskId("C"), duration = Duration(1)),
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

        val result = CriticalPathAnalysis(plan, calendar).compute()

        assertEquals(3, result.tasks.size)
        // All tasks should be critical
        for (id in listOf("A", "B", "C")) {
            val task = result.tasks[taskId(id)]!!
            assertEquals(0, task.slack, "Task $id should have slack=0")
            assertTrue(task.isCritical, "Task $id should be critical")
        }
        assertEquals(setOf(taskId("A"), taskId("B"), taskId("C")), result.criticalTaskIds)
    }

    @Test
    fun diamond_parallelPaths_longerPathIsCritical() {
        // A(1d) -> B(3d) -> D(1d)
        // A(1d) -> C(1d) -> D(1d)
        // Path A-B-D is longer than A-C-D, so A,B,D are critical, C has slack
        // A: Mon Mar 3
        // FS: B starts Mar 4, dur 3 -> ends Mar 6 (Thu)
        // FS: C starts Mar 4, dur 1 -> ends Mar 4 (Tue)
        // FS: D starts after max(B end, C end) = Mar 6 -> D starts Mar 6, dur 1 -> ends Mar 6
        // Wait: FS lag=0: constrained start = addWorkingDays(predEnd, 2)
        // B end = Mar 6 -> D constrained = addWorkingDays(Mar6, 2) = Mar 7 (Fri)
        // C end = Mar 4 -> D constrained = addWorkingDays(Mar4, 2) = Mar 5 (Wed)
        // D start = max(Mar7, Mar5) = Mar 7, dur 1, end = Mar 7
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(3)),
                ProjectTask(id = taskId("C"), duration = Duration(1)),
                ProjectTask(id = taskId("D"), duration = Duration(1)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 6))),
                TaskSchedule(id = schedId("C"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
                TaskSchedule(id = schedId("D"), start = ProjectDate.Set(LocalDate(2025, 3, 7)), end = ProjectDate.Set(LocalDate(2025, 3, 7))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
                TaskDependency(predecessor = taskId("A"), successor = taskId("C"), type = DependencyType.FS),
                TaskDependency(predecessor = taskId("B"), successor = taskId("D"), type = DependencyType.FS),
                TaskDependency(predecessor = taskId("C"), successor = taskId("D"), type = DependencyType.FS),
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        // A, B, D are critical (longer path)
        assertTrue(result.tasks[taskId("A")]!!.isCritical, "A should be critical")
        assertTrue(result.tasks[taskId("B")]!!.isCritical, "B should be critical")
        assertTrue(result.tasks[taskId("D")]!!.isCritical, "D should be critical")

        // C has slack
        assertFalse(result.tasks[taskId("C")]!!.isCritical, "C should NOT be critical")
        assertTrue(result.tasks[taskId("C")]!!.slack > 0, "C should have positive slack")
    }

    @Test
    fun ssDependency() {
        // A(3d) -> B(2d) via SS lag=0
        // A: Mon Mar 3 -> Wed Mar 5
        // SS lag=0: B start = addWorkingDays(A start=Mar3, 0) = Mar 3
        // B dur=2 end = addWorkingDays(Mar3, 2) = Mar 4
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.SS),
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        val a = result.tasks[taskId("A")]!!
        val b = result.tasks[taskId("B")]!!
        assertEquals(LocalDate(2025, 3, 3), b.es)
        assertEquals(LocalDate(2025, 3, 4), b.ef)
        // Project end = max(Mar 5, Mar 4) = Mar 5
        assertEquals(LocalDate(2025, 3, 5), result.projectEnd)
        // A is critical (ends at project end), B has slack
        assertTrue(a.isCritical)
        assertTrue(b.slack > 0)
    }

    @Test
    fun ffDependency() {
        // A(3d) -> B(2d) via FF lag=0
        // A: Mon Mar 3 -> Wed Mar 5
        // FF lag=0: B end = addWorkingDays(A end=Mar5, 0) = Mar 5
        // B dur=2: B start = subtractWorkingDays(Mar5, 2) = Mar 4
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FF),
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        val b = result.tasks[taskId("B")]!!
        assertEquals(LocalDate(2025, 3, 4), b.es)
        assertEquals(LocalDate(2025, 3, 5), b.ef)
        assertEquals(LocalDate(2025, 3, 5), result.projectEnd)
    }

    @Test
    fun fsWithLag() {
        // A(1d) -> B(1d) via FS lag=2
        // A: Mon Mar 3 (end Mar 3)
        // FS lag=2: n = 2+2 = 4, B start = addWorkingDays(Mar3, 4) = Mar 6 (Thu)
        // B dur=1 end = Mar 6
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 6)), end = ProjectDate.Set(LocalDate(2025, 3, 6))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS, lag = Duration(2)),
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        val a = result.tasks[taskId("A")]!!
        val b = result.tasks[taskId("B")]!!
        assertEquals(LocalDate(2025, 3, 6), b.es)
        assertEquals(LocalDate(2025, 3, 6), b.ef)
        // Both are critical — lag is consumed, doesn't create slack
        assertTrue(b.isCritical, "B should be critical")
        assertTrue(a.isCritical, "A should be critical")
    }

    @Test
    fun unscheduledTask_excluded() {
        // A scheduled, B unscheduled, A -> B dependency
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.NotSet, end = ProjectDate.NotSet),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        assertEquals(1, result.tasks.size)
        assertTrue(taskId("A") in result.tasks)
        assertTrue(taskId("A") in result.criticalTaskIds)
    }

    @Test
    fun independentTasks_shortestHasSlack() {
        // A(3d), B(1d), C(2d), no dependencies
        // A: Mon Mar 3 -> Wed Mar 5
        // B: Mon Mar 3
        // C: Mon Mar 3 -> Tue Mar 4
        // projectEnd = Mar 5
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
                ProjectTask(id = taskId("C"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("C"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
        )

        val result = CriticalPathAnalysis(plan, calendar).compute()

        assertEquals(LocalDate(2025, 3, 5), result.projectEnd)
        // A is longest -> its LF = projectEnd = Mar 5, LS = subtractWorkingDays(Mar5, 3) = Mar 3
        // A: ES=Mar3, LS=Mar3, slack=0, critical
        assertTrue(result.tasks[taskId("A")]!!.isCritical)
        // B(dur=1): LF = Mar 5, LS = subtractWorkingDays(Mar5, 1) = Mar 5
        // B: ES=Mar3, LS=Mar5, slack = workingDaysBetween(Mar3, Mar5) - 1 = 3 - 1 = 2
        assertEquals(2, result.tasks[taskId("B")]!!.slack)
        assertFalse(result.tasks[taskId("B")]!!.isCritical)
        // C(dur=2): LF = Mar 5, LS = subtractWorkingDays(Mar5, 2) = Mar 4
        // C: ES=Mar3, LS=Mar4, slack = workingDaysBetween(Mar3, Mar4) - 1 = 2 - 1 = 1
        assertEquals(1, result.tasks[taskId("C")]!!.slack)
        assertFalse(result.tasks[taskId("C")]!!.isCritical)
    }
}
