package com.khan366kos.atlas.project.backend.common.models.timelineCalendar

import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TimelineCalendarTest {

    private val calendar = TimelineCalendar(
        workingWeekDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        ),
        weekendWeekDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        holidays = setOf(LocalDate(2025, 3, 7)), // Friday holiday
        workingWeekends = emptySet(),
    )

    // --- addWorkingDays ---

    @Test
    fun addWorkingDays_normalCase() {
        // Monday + 3 days = Wednesday
        val monday = LocalDate(2025, 3, 3)
        val result = calendar.addWorkingDays(monday, Duration(3))
        assertEquals(LocalDate(2025, 3, 5), result) // Wednesday
    }

    @Test
    fun addWorkingDays_acrossWeekend() {
        // Friday + 2 days = Tuesday (skips Sat, Sun)
        val friday = LocalDate(2025, 3, 14)
        val result = calendar.addWorkingDays(friday, Duration(2))
        assertEquals(LocalDate(2025, 3, 17), result) // Monday
    }

    @Test
    fun addWorkingDays_acrossHoliday() {
        // Thursday Mar 6 + 2 = Monday Mar 10 (skips Friday holiday + weekend)
        val thursday = LocalDate(2025, 3, 6)
        val result = calendar.addWorkingDays(thursday, Duration(2))
        assertEquals(LocalDate(2025, 3, 10), result) // Monday
    }

    @Test
    fun addWorkingDays_duration0() {
        val monday = LocalDate(2025, 3, 3)
        val result = calendar.addWorkingDays(monday, Duration(0))
        assertEquals(monday, result)
    }

    @Test
    fun addWorkingDays_duration1() {
        // Duration 1 from a working day = same day
        val monday = LocalDate(2025, 3, 3)
        val result = calendar.addWorkingDays(monday, Duration(1))
        assertEquals(monday, result)
    }

    @Test
    fun addWorkingDays_startOnWeekend() {
        // Saturday + 1 = Monday (skips to next working day)
        val saturday = LocalDate(2025, 3, 8)
        val result = calendar.addWorkingDays(saturday, Duration(1))
        assertEquals(LocalDate(2025, 3, 10), result) // Monday
    }

    // --- subtractWorkingDays ---

    @Test
    fun subtractWorkingDays_normalCase() {
        // Wednesday - 3 = Monday
        val wednesday = LocalDate(2025, 3, 5)
        val result = calendar.subtractWorkingDays(wednesday, Duration(3))
        assertEquals(LocalDate(2025, 3, 3), result)
    }

    @Test
    fun subtractWorkingDays_acrossWeekend() {
        // Monday - 2 = Thursday (skips weekend)
        val monday = LocalDate(2025, 3, 17)
        val result = calendar.subtractWorkingDays(monday, Duration(2))
        assertEquals(LocalDate(2025, 3, 14), result) // Friday
    }

    @Test
    fun subtractWorkingDays_acrossHoliday() {
        // Monday Mar 10 - 2 = Thursday Mar 6 (skips Fri holiday + weekend)
        val monday = LocalDate(2025, 3, 10)
        val result = calendar.subtractWorkingDays(monday, Duration(2))
        assertEquals(LocalDate(2025, 3, 6), result)
    }

    // --- workingDaysBetween ---

    @Test
    fun workingDaysBetween_normalCase() {
        // Mon to Wed = 3 working days
        val monday = LocalDate(2025, 3, 3)
        val wednesday = LocalDate(2025, 3, 5)
        assertEquals(Duration(3), calendar.workingDaysBetween(monday, wednesday))
    }

    @Test
    fun workingDaysBetween_acrossWeekend() {
        // Friday to Tuesday = 3 working days (Fri, Mon, Tue)
        val friday = LocalDate(2025, 3, 14)
        val tuesday = LocalDate(2025, 3, 18)
        assertEquals(Duration(3), calendar.workingDaysBetween(friday, tuesday))
    }

    @Test
    fun workingDaysBetween_endBeforeStart() {
        val wednesday = LocalDate(2025, 3, 5)
        val monday = LocalDate(2025, 3, 3)
        assertEquals(Duration.ZERO, calendar.workingDaysBetween(wednesday, monday))
    }

    // --- currentOrNextWorkingDay ---

    @Test
    fun currentOrNextWorkingDay_workingDay() {
        val monday = LocalDate(2025, 3, 3)
        assertEquals(monday, calendar.currentOrNextWorkingDay(monday))
    }

    @Test
    fun currentOrNextWorkingDay_weekend() {
        val saturday = LocalDate(2025, 3, 8)
        assertEquals(LocalDate(2025, 3, 10), calendar.currentOrNextWorkingDay(saturday)) // Monday
    }

    @Test
    fun currentOrNextWorkingDay_holiday() {
        val holiday = LocalDate(2025, 3, 7) // Friday holiday
        assertEquals(LocalDate(2025, 3, 10), calendar.currentOrNextWorkingDay(holiday)) // Monday
    }
}
