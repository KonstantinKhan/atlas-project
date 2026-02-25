package com.khan366kos.atlas.project.backend.repo.postgres.mapper

import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarWorkingWeekendsTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toWorkingWeekend(): LocalDate = this[TimelineCalendarWorkingWeekendsTable.workingDate]
