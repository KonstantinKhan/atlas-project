package com.khan366kos.atlas.project.backend.transport.simple

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class PlannedCalendarDuration(val value: String) {
    constructor(value: Int) : this(value.toString())

    fun asInt(): Int = value.toInt()

    fun asString(): String = value

    companion object {
        val NONE: PlannedCalendarDuration = PlannedCalendarDuration("")
    }
}
