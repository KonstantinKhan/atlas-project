package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.transport.ChangeTaskStartDateCommandDto

fun ChangeTaskStartDateCommandDto.toDomain() = TaskSchedule(
    id = TaskScheduleId(taskId),
    start = ProjectDate.Set(newPlannedStart),
    end = TODO()
)