package com.khan366kos.atlas.project.backend.common.models.project

import java.util.UUID

@JvmInline
value class ProjectId (private val value: String) {

    constructor(id: UUID) : this(id.toString())

    fun asString() = value

    companion object {
        val NONE = ProjectId("")
    }
}