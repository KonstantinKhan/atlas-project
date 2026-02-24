package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.CalendarDuration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus as CommonProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskDescription
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskTitle
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import kotlinx.datetime.LocalDate
import com.khan366kos.atlas.project.backend.transport.ProjectTaskTransport
import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus as TransportProjectTaskStatus
import java.util.UUID

fun ProjectTask.toTransport() = ProjectTaskTransport(
    id = projectTaskId.value,
    title = projectTaskTitle.value,
    description = projectTaskDescription.value,
    plannedCalendarDuration = plannedCalendarDuration.value.toIntOrNull(),
    actualCalendarDuration = actualCalendarDuration.value.toIntOrNull(),
    plannedStartDate = when (val d = plannedStartDate) {
        is ProjectDate.Set -> d.date
        ProjectDate.NotSet -> null
    },
    plannedEndDate = when (val d = plannedEndDate) {
        is ProjectDate.Set -> d.date
        ProjectDate.NotSet -> null
    },
    actualStartDate = when (val d = actualStartDate) {
        is ProjectDate.Set -> d.date
        ProjectDate.NotSet -> null
    },
    actualEndDate = when (val d = actualEndDate) {
        is ProjectDate.Set -> d.date
        ProjectDate.NotSet -> null
    },
    status = TransportProjectTaskStatus.valueOf(status.name),
    dependsOn = dependsOn.map { it.value },
    dependsOnLag = dependsOnLag.map { (k, v) -> k.value to v.asInt() }.toMap(),
)

fun ProjectTask.applyUpdate(req: UpdateProjectTaskRequest, resolvedEndDate: LocalDate?) = copy(
    projectTaskTitle = req.title?.let { ProjectTaskTitle(it) } ?: projectTaskTitle,
    projectTaskDescription = req.description?.let { ProjectTaskDescription(it) } ?: projectTaskDescription,
    plannedCalendarDuration = req.plannedCalendarDuration?.let { CalendarDuration(it.toString()) } ?: plannedCalendarDuration,
    actualCalendarDuration = req.actualCalendarDuration?.let { CalendarDuration(it.toString()) } ?: actualCalendarDuration,
    plannedStartDate = req.plannedStartDate?.let { ProjectDate.Set(it) } ?: plannedStartDate,
    plannedEndDate = resolvedEndDate?.let { ProjectDate.Set(it) } ?: plannedEndDate,
    actualStartDate = req.actualStartDate?.let { ProjectDate.Set(it) } ?: actualStartDate,
    actualEndDate = req.actualEndDate?.let { ProjectDate.Set(it) } ?: actualEndDate,
    status = req.status?.let { CommonProjectTaskStatus.valueOf(it.name) } ?: status,
    dependsOn = req.dependsOn?.map { ProjectTaskId(it) } ?: dependsOn,
    dependsOnLag = req.dependsOnLag?.map { (k, v) -> ProjectTaskId(k) to CalendarDuration(v.toString()) }?.toMap() ?: dependsOnLag,
)

fun CreateProjectTaskRequest.toModel() = ProjectTask(
    projectTaskId = ProjectTaskId(UUID.randomUUID().toString()),
    projectTaskTitle = ProjectTaskTitle(title),
    projectTaskDescription = ProjectTaskDescription(description),
    plannedCalendarDuration = plannedCalendarDuration?.let { CalendarDuration(it.toString()) } ?: CalendarDuration.NONE,
    actualCalendarDuration = actualCalendarDuration?.let { CalendarDuration(it.toString()) } ?: CalendarDuration.NONE,
    plannedStartDate = plannedStartDate?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    plannedEndDate = plannedEndDate?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    actualStartDate = actualStartDate?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    actualEndDate = actualEndDate?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    status = CommonProjectTaskStatus.valueOf(status.name),
    dependsOn = dependsOn.map { ProjectTaskId(it) },
    dependsOnLag = dependsOnLag.map { (k, v) -> ProjectTaskId(k) to CalendarDuration(v.toString()) }.toMap(),
)