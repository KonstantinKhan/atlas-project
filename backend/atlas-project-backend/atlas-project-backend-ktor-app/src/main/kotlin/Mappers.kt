package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.transport.ProjectTaskTransport
import com.khan366kos.atlas.project.backend.transport.enums.ProjectTaskStatus as TransportProjectTaskStatus

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