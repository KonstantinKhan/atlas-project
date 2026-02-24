package com.khan366kos.atlas.project.backend.common.models.task.simple

@JvmInline
value class ProjectTaskDescription(val value: String)  {
    fun asString(): String  = value

    companion object {
        val NONE = ProjectTaskDescription("")
    }
}