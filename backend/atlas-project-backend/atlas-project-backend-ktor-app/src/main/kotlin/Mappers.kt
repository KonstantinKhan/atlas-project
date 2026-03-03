package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus as CommonProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.TaskDto
import com.khan366kos.atlas.project.backend.transport.ScheduledTaskDto
import com.khan366kos.atlas.project.backend.transport.commands.CreateTaskInPoolCommandDto
import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus as TransportProjectTaskStatus
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

fun ProjectTask.toTaskDto() = TaskDto(
    id = id.value,
    title = title.value,
    description = description.value,
    status = TransportProjectTaskStatus.valueOf(status.name),
)

fun ProjectTask.toScheduledTaskDto(
    startDate: kotlinx.datetime.LocalDate,
    endDate: kotlinx.datetime.LocalDate
) = ScheduledTaskDto(
    id = id.value,
    title = title.value,
    description = description.value,
    plannedStartDate = startDate,
    plannedEndDate = endDate,
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

@OptIn(ExperimentalUuidApi::class)
fun CreateTaskInPoolCommandDto.toModel() = ProjectTask(
    id = TaskId(Uuid.random()),
    title = Title(title),
)
