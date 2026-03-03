package com.khan366kos.atlas.project.backend.transport.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTaskStatus {
    EMPTY,
    BACKLOG,
    IN_PROGRESS,
    DONE,
    BLOCKED,
}