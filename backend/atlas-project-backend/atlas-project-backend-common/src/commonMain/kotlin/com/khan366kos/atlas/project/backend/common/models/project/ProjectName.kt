package com.khan366kos.atlas.project.backend.common.models.project

@JvmInline
value class ProjectName(private val value: String) {
    fun asString() = value

    companion object {
        val NONE = ProjectName("")
    }
}