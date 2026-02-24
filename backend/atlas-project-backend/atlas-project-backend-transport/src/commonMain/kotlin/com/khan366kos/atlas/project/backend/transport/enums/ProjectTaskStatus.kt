package com.khan366kos.atlas.project.backend.transport.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTaskStatus(val value: String) {
    EMPTY("empty"),
    BACKLOG("backlog"),
    IN_PROGRESS("in progress"),
    DONE("done"),
    BLOCKED("blocked"),
}