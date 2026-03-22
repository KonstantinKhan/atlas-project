package com.khan366kos.atlas.project.backend.common.models.task

import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import kotlinx.datetime.LocalDate

data class ProjectTask(
    val id: TaskId = TaskId.NONE,
    val title: Title = Title.NONE,
    val description: Description = Description.NONE,
    val duration: Duration = Duration.ZERO,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
    val sortOrder: Int = 0,
    val baselineStart: LocalDate? = null,
    val baselineEnd: LocalDate? = null,
    val actualStart: LocalDate? = null,
    val actualEnd: LocalDate? = null,
    val baselineEffortHours: Double? = null,
    val additionalEffortHours: Double? = null,
)
