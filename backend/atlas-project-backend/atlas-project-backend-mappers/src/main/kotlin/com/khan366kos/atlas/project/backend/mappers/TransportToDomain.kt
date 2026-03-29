package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskEndDateCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskStartDateCommandDto
import com.khan366kos.atlas.project.backend.transport.enums.DependencyTypeDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectPriorityDto

fun ChangeTaskStartDateCommandDto.toDomain() = TaskSchedule(
    id = TaskScheduleId(taskId),
    start = ProjectDate.Set(newPlannedStart),
)

fun ChangeTaskEndDateCommandDto.toDomain() = TaskSchedule(
    id = TaskScheduleId(taskId),
    end = ProjectDate.Set(newPlannedEnd),
)

fun DependencyTypeDto.toDomain() = DependencyType.valueOf(this.name)

fun ProjectPriorityDto.toDomain() = ProjectPriority.valueOf(this.name)