package com.khan366kos.atlas.project.backend.common.models.task.simple

@JvmInline
value class ProjectTaskTitle(val value: String) {
    fun asString(): String = value

    companion object {
        val NONE: ProjectTaskTitle = ProjectTaskTitle("")
    }
}