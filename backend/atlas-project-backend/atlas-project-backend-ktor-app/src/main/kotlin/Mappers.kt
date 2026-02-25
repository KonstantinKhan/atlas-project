package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus as CommonProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.ProjectTaskTransport
import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus as TransportProjectTaskStatus
import java.util.UUID

fun ProjectTask.toTransport() = ProjectTaskTransport(
    id = id.value,
    title = title.value,
    description = description.value,
    plannedCalendarDuration = duration.value.toIntOrNull(),
    status = TransportProjectTaskStatus.valueOf(status.name),
)

fun ProjectTask.applyUpdate(req: UpdateProjectTaskRequest) = copy(
    title = req.title?.let { Title(it) } ?: title,
    description = req.description?.let { Description(it) } ?: description,
    duration = req.plannedCalendarDuration?.let { Duration(it.toString()) } ?: duration,
    status = req.status?.let { CommonProjectTaskStatus.valueOf(it.name) } ?: status,
)

fun CreateProjectTaskRequest.toModel() = ProjectTask(
    id = TaskId(UUID.randomUUID().toString()),
    title = Title(title),
    description = Description(description),
    duration = plannedCalendarDuration?.let { Duration(it.toString()) } ?: Duration.NONE,
    status = CommonProjectTaskStatus.valueOf(status.name),
)
