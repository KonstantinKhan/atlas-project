package com.khan366kos.atlas.project.backend.common.models.resource

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class AssignmentId(val value: String) {
    @OptIn(ExperimentalUuidApi::class)
    constructor(uuid: Uuid) : this(uuid.toString())

    fun asString(): String = value
    companion object {
        val NONE: AssignmentId = AssignmentId("")
    }
}
