package com.khan366kos.atlas.project.backend.common.models.simple

@JvmInline
value class CalendarDuration(val value: String) {
    fun asInt(): Int = value.toInt()
    companion object {
        val NONE = CalendarDuration("")
    }
}