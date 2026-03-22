package com.khan366kos.atlas.project.backend.common.models.resource

import kotlinx.datetime.LocalDate

data class AssignmentDayOverride(
    val assignmentId: AssignmentId,
    val date: LocalDate,
    val hours: Double,
) {
    companion object {
        val NONE: AssignmentDayOverride = AssignmentDayOverride(
            assignmentId = AssignmentId.NONE,
            date = LocalDate(2000, 1, 1),
            hours = 0.0,
        )
    }
}
