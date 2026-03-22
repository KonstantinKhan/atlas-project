package com.khan366kos.atlas.project.backend.common.project

@JvmInline
value class ProjectId (private val value: String) {
    fun asString() = value

    companion object {
        val NONE = ProjectId("")
    }
}