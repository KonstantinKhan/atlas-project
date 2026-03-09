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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class ProjectPlanTest {

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

    // --- changeTaskStartDate ---

    @Test
    fun changeTaskStartDate_singleTask() {
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

        val delta = plan.changeTaskStartDate(taskId("A"), LocalDate(2025, 3, 10), calendar)

        assertEquals(1, delta.updatedSchedule.size)
        val sched = delta.updatedSchedule[0]
        assertEquals(LocalDate(2025, 3, 10), (sched.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 12), (sched.end as ProjectDate.Set).date)
    }

    @Test
    fun changeTaskStartDate_cascadeThroughFS() {
        // A(3d) -> B(2d), FS dependency
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 6)), end = ProjectDate.Set(LocalDate(2025, 3, 7))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        // Move A start from Mon Mar 3 to Mon Mar 10
        val delta = plan.changeTaskStartDate(taskId("A"), LocalDate(2025, 3, 10), calendar)

        // A: Mar 10 (Mon) -> Mar 12 (Wed), duration 3
        // FS lag=0: B starts = addWorkingDays(predEnd=Mar12, 1) = Mar 12
        // B dur=2, end = addWorkingDays(Mar 12, 2) = Mar 13
        assertEquals(2, delta.updatedSchedule.size)
        val schedB = plan.schedules()[schedId("B")]!!
        assertEquals(LocalDate(2025, 3, 12), (schedB.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 13), (schedB.end as ProjectDate.Set).date)
    }

    @Test
    fun changeTaskStartDate_cascadeChain_ABC() {
        // A -> B -> C, all FS
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

        // Move A to Wednesday Mar 5
        val delta = plan.changeTaskStartDate(taskId("A"), LocalDate(2025, 3, 5), calendar)

        // A: dur=1, start=Mar 5, end=Mar 5
        // FS lag=0: B starts = addWorkingDays(Mar 5, 1) = Mar 5, end=Mar 5
        // FS lag=0: C starts = addWorkingDays(Mar 5, 1) = Mar 5, end=Mar 5
        assertEquals(3, delta.updatedSchedule.size)
        val schedB = plan.schedules()[schedId("B")]!!
        val schedC = plan.schedules()[schedId("C")]!!
        assertEquals(LocalDate(2025, 3, 5), (schedB.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 5), (schedC.start as ProjectDate.Set).date)
    }

    // --- changeTaskEndDate ---

    @Test
    fun changeTaskEndDate_recalculatesDuration() {
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

        // Change end from Wed to Fri => duration changes from 3 to 5
        val delta = plan.changeTaskEndDate(taskId("A"), LocalDate(2025, 3, 7), calendar)

        assertEquals(1, delta.updatedSchedule.size)
        val sched = delta.updatedSchedule[0]
        assertEquals(LocalDate(2025, 3, 3), (sched.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 7), (sched.end as ProjectDate.Set).date)
    }

    @Test
    fun changeTaskEndDate_cascade() {
        // A -> B, FS
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 6)), end = ProjectDate.Set(LocalDate(2025, 3, 7))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        // Extend A end from Wed Mar 5 to Fri Mar 7 => B should move
        val delta = plan.changeTaskEndDate(taskId("A"), LocalDate(2025, 3, 7), calendar)

        // FS lag=0: B starts = addWorkingDays(Mar 7 Fri, 1) = Mar 7
        val schedB = plan.schedules()[schedId("B")]!!
        assertEquals(LocalDate(2025, 3, 7), (schedB.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 10), (schedB.end as ProjectDate.Set).date) // dur=2: Fri+Mon
    }

    // --- addDependency ---

    @Test
    fun addDependency_FS_recalculatesSuccessor() {
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
        )

        // B starts at same time as A, adding FS dep should push B after A end
        // FS lag=0 (calculated): succStart <= predEnd, so lag=0
        // constrainedStart = addWorkingDays(predEnd=Mar 5, 1) = Mar 5
        val delta = plan.addDependency(taskId("A"), taskId("B"), DependencyType.FS, null, calendar)

        val schedB = plan.schedules()[schedId("B")]!!
        assertEquals(LocalDate(2025, 3, 5), (schedB.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 6), (schedB.end as ProjectDate.Set).date)
    }

    @Test
    fun addDependency_cycle_throwsError() {
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        assertFailsWith<IllegalStateException> {
            plan.addDependency(taskId("B"), taskId("A"), DependencyType.FS, null, calendar)
        }
    }

    @Test
    fun addDependency_multiplePredecessors_takesMax() {
        // A(3d) and C(5d) both predecessors of B(2d) via FS
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
                ProjectTask(id = taskId("C"), duration = Duration(5)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
                TaskSchedule(id = schedId("C"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 7))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        // Add second FS dependency C -> B. C ends later, so B should start after C
        // FS lag=0: constrainedStart = addWorkingDays(predEnd=Mar 7 Fri, 1) = Mar 7
        // Max of A constraint (Mar 5) and C constraint (Mar 7) = Mar 7
        plan.addDependency(taskId("C"), taskId("B"), DependencyType.FS, null, calendar)

        val schedB = plan.schedules()[schedId("B")]!!
        assertEquals(LocalDate(2025, 3, 7), (schedB.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 10), (schedB.end as ProjectDate.Set).date) // dur=2: Fri+Mon
    }

    // --- validateNoCycles ---

    @Test
    fun validateNoCycles_validGraph_returnsTrue() {
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        assertTrue(plan.validateNoCycles())
    }

    // --- snapshot ---

    @Test
    fun snapshot_createsIndependentCopy() {
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(3)),
                ProjectTask(id = taskId("B"), duration = Duration(2)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 6)), end = ProjectDate.Set(LocalDate(2025, 3, 7))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
            ),
        )

        val snap = plan.snapshot()

        // Mutate the original plan
        plan.changeTaskStartDate(taskId("A"), LocalDate(2025, 3, 10), calendar)

        // Snapshot should be unaffected
        val snapSchedA = snap.schedules()[schedId("A")]!!
        assertEquals(LocalDate(2025, 3, 3), (snapSchedA.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 5), (snapSchedA.end as ProjectDate.Set).date)

        // Original should be changed
        val origSchedA = plan.schedules()[schedId("A")]!!
        assertEquals(LocalDate(2025, 3, 10), (origSchedA.start as ProjectDate.Set).date)
    }

    // --- recalculateAll (regression) ---

    @Test
    fun recalculateAll_chain_sameResult() {
        // A -> B -> C, all FS, duration 1
        val plan = buildPlan(
            tasks = listOf(
                ProjectTask(id = taskId("A"), duration = Duration(1)),
                ProjectTask(id = taskId("B"), duration = Duration(1)),
                ProjectTask(id = taskId("C"), duration = Duration(1)),
            ),
            schedules = listOf(
                TaskSchedule(id = schedId("A"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("B"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
                TaskSchedule(id = schedId("C"), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
            ),
            dependencies = setOf(
                TaskDependency(predecessor = taskId("A"), successor = taskId("B"), type = DependencyType.FS),
                TaskDependency(predecessor = taskId("B"), successor = taskId("C"), type = DependencyType.FS),
            ),
        )

        val delta = plan.recalculateAll(calendar)

        // B and C should be recalculated (A is root, skipped)
        assertEquals(2, delta.updatedSchedule.size)

        val schedB = plan.schedules()[schedId("B")]!!
        val schedC = plan.schedules()[schedId("C")]!!

        // FS lag=0: B starts after A end (Mar 3), dur=1 => start=Mar 3, end=Mar 3
        // Actually FS lag=0: constrainedStart = addWorkingDays(predEnd=Mar3, Duration(2)) ... let me check
        // From calculateConstrainedStart: FS lag=0 => n = 0+2 = 2, addWorkingDays(Mar3, Duration(2)) = Mar 4
        assertEquals(LocalDate(2025, 3, 4), (schedB.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 4), (schedB.end as ProjectDate.Set).date)

        assertEquals(LocalDate(2025, 3, 5), (schedC.start as ProjectDate.Set).date)
        assertEquals(LocalDate(2025, 3, 5), (schedC.end as ProjectDate.Set).date)
    }
}
