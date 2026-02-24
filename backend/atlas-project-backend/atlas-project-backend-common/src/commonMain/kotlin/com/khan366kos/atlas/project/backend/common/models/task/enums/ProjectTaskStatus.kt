package com.khan366kos.atlas.project.backend.common.models.task.enums

enum class ProjectTaskStatus(val value: String) {
    EMPTY("empty"),
    BACKLOG("backlog"),
    IN_PROGRESS("in progress"),
    DONE("done"),
    BLOCKED("blocked"),
}