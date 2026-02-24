package com.khan366kos.atlas.project.backend.common.models.task

import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.CalendarDuration
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskDescription
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskTitle
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskId

data class ProjectTask(
    val projectTaskId: ProjectTaskId = ProjectTaskId.NONE,
    val projectTaskTitle: ProjectTaskTitle = ProjectTaskTitle.NONE,
    val projectTaskDescription: ProjectTaskDescription = ProjectTaskDescription.NONE,
    val plannedCalendarDuration: CalendarDuration = CalendarDuration.NONE,
    val actualCalendarDuration: CalendarDuration = CalendarDuration.NONE,
    val plannedStartDate: ProjectDate = ProjectDate.NotSet,
    val actualStartDate: ProjectDate = ProjectDate.NotSet,
    val plannedEndDate: ProjectDate = ProjectDate.NotSet,
    val actualEndDate: ProjectDate = ProjectDate.NotSet,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
    val dependsOn: List<ProjectTaskId> = emptyList(),
    val dependsOnLag: Map<ProjectTaskId, CalendarDuration> = emptyMap(),
)
