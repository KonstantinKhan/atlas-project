package com.khan366kos.atlas.project.backend.common.models.task.simple

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class TaskId(val value: String) {
    @OptIn(ExperimentalUuidApi::class)
    constructor(uuid: Uuid) : this(uuid.toString())

    fun asString(): String = value
    companion object {
        val NONE: TaskId = TaskId("")
    }
}