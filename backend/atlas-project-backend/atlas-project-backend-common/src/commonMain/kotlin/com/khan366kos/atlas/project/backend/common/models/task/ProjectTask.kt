package com.khan366kos.atlas.project.backend.common.models.task

import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId

data class ProjectTask(
    val id: TaskId = TaskId.NONE,
    val title: Title = Title.NONE,
    val description: Description = Description.NONE,
    val duration: Duration = Duration.NONE,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
)
