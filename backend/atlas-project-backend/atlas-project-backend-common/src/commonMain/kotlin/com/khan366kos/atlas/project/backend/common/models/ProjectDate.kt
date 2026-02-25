package com.khan366kos.atlas.project.backend.common.models

import kotlinx.datetime.LocalDate

sealed class ProjectDate {
    object NotSet: ProjectDate()
    data class Set(val date: LocalDate) : ProjectDate() {
        fun asLocalDate() = date
    }
}