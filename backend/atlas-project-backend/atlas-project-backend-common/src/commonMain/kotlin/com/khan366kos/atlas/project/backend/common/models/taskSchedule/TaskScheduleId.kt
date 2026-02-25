package com.khan366kos.atlas.project.backend.common.models.taskSchedule

@JvmInline
value class TaskScheduleId(val value: String) {
    fun asString() = value
    companion object {
        val NONE = TaskScheduleId("")
    }
}