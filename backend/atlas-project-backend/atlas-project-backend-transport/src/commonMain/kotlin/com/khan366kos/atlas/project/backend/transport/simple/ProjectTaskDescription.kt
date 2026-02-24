package com.khan366kos.atlas.project.backend.transport.simple

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ProjectTaskDescription(val value: String) {
    fun asString(): String = value
    companion object {
        val NONE: ProjectTaskDescription = ProjectTaskDescription("")
    }
}
