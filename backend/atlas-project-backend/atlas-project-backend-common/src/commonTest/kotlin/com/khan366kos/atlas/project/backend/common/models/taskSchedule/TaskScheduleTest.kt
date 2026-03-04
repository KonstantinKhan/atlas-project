package com.khan366kos.atlas.project.backend.common.models.taskSchedule

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TaskScheduleTest {

    @Test
    fun createWithSetDates() {
        val schedule = TaskSchedule(
            id = TaskScheduleId("task-1"),
            start = ProjectDate.Set(LocalDate(2025, 3, 3)),
            end = ProjectDate.Set(LocalDate(2025, 3, 5)),
        )

        val start = assertIs<ProjectDate.Set>(schedule.start)
        val end = assertIs<ProjectDate.Set>(schedule.end)
        assertEquals(LocalDate(2025, 3, 3), start.date)
        assertEquals(LocalDate(2025, 3, 5), end.date)
    }

    @Test
    fun createWithNotSetDates() {
        val schedule = TaskSchedule(
            id = TaskScheduleId("task-2"),
            start = ProjectDate.NotSet,
            end = ProjectDate.NotSet,
        )

        assertIs<ProjectDate.NotSet>(schedule.start)
        assertIs<ProjectDate.NotSet>(schedule.end)
    }
}
