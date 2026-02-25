package com.khan366kos.atlas.project.backend.common.models.taskSchedule

import com.khan366kos.atlas.project.backend.common.models.ProjectDate

data class TaskSchedule(
    val id: TaskScheduleId = TaskScheduleId.NONE,
    val start: ProjectDate = ProjectDate.NotSet,
    val end: ProjectDate = ProjectDate.NotSet,
)
