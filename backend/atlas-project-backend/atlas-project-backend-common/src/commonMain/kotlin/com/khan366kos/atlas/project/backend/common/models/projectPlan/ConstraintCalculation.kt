package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import kotlinx.datetime.LocalDate

/**
 * Forward pass: given a dependency and predecessor schedule,
 * compute the earliest allowed start for the successor.
 */
fun calculateConstrainedStart(
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

/**
 * Backward pass: given a dependency and successor's LS/LF,
 * compute the latest allowed finish (LF) for the predecessor.
 */
fun calculateConstrainedLF(
    dep: TaskDependency,
    succLS: LocalDate,
    succLF: LocalDate,
    predDuration: Duration,
    calendar: TimelineCalendar,
): LocalDate {
    val lag = dep.lag.asInt()
    return when (dep.type) {
        DependencyType.FS -> {
            // Forward: succStart = addWorkingDays(predEnd, lag+2)
            // Backward: predEnd <= subtractWorkingDays(succLS, lag+2)
            val n = lag + 2
            if (n > 0) calendar.subtractWorkingDays(succLS, Duration(n))
            else calendar.addWorkingDays(succLS, Duration(-lag))
        }
        DependencyType.SS -> {
            // Forward: succStart = addWorkingDays(predStart, lag)
            // Backward: predStart <= subtractWorkingDays(succLS, lag) → predLF = addWorkingDays(predLS, predDuration)
            val predLS = calendar.subtractWorkingDays(succLS, Duration(lag))
            calendar.addWorkingDays(predLS, predDuration)
        }
        DependencyType.FF -> {
            // Forward: succEnd = addWorkingDays(predEnd, lag)
            // Backward: predEnd <= subtractWorkingDays(succLF, lag)
            calendar.subtractWorkingDays(succLF, Duration(lag))
        }
        DependencyType.SF -> {
            // Forward: succEnd = addWorkingDays(predStart, lag)
            // Backward: predStart <= subtractWorkingDays(succLF, lag) → predLF = addWorkingDays(predLS, predDuration)
            val predLS = calendar.subtractWorkingDays(succLF, Duration(lag))
            calendar.addWorkingDays(predLS, predDuration)
        }
    }
}
