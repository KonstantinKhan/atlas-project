package com.khan366kos.atlas.project.backend.repo.postgres.mapper

import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarHolidaysTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toHoliday(): LocalDate = this[TimelineCalendarHolidaysTable.holidayDate]
