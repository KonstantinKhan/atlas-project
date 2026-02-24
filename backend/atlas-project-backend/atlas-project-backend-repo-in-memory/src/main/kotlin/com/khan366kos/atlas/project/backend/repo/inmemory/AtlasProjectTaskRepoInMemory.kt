package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.CalendarDuration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskDescription
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.ProjectTaskTitle
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AtlasProjectTaskRepoInMemory(private val database: Database) : IAtlasProjectTaskRepo {
    override suspend fun tasks(): List<ProjectTask> = newSuspendedTransaction(db = database) {
        ProjectTasksTable.selectAll().map { it.toProjectTask() }
    }
}

private fun ResultRow.toProjectTask() = ProjectTask(
    projectTaskId = ProjectTaskId(this[ProjectTasksTable.id]),
    projectTaskTitle = ProjectTaskTitle(this[ProjectTasksTable.title]),
    projectTaskDescription = ProjectTaskDescription(this[ProjectTasksTable.description]),
    plannedCalendarDuration = this[ProjectTasksTable.plannedCalendarDuration]
        ?.let { CalendarDuration(it.toString()) } ?: CalendarDuration.NONE,
    actualCalendarDuration = this[ProjectTasksTable.actualCalendarDuration]
        ?.let { CalendarDuration(it.toString()) } ?: CalendarDuration.NONE,
    plannedStartDate = this[ProjectTasksTable.plannedStartDate]
        ?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    plannedEndDate = this[ProjectTasksTable.plannedEndDate]
        ?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    actualStartDate = this[ProjectTasksTable.actualStartDate]
        ?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    actualEndDate = this[ProjectTasksTable.actualEndDate]
        ?.let { ProjectDate.Set(it) } ?: ProjectDate.NotSet,
    status = ProjectTaskStatus.valueOf(this[ProjectTasksTable.status]),
    dependsOn = this[ProjectTasksTable.dependsOn]
        .split(",").filter { it.isNotBlank() }.map { ProjectTaskId(it) },
    dependsOnLag = this[ProjectTasksTable.dependsOnLag]
        .split(",").filter { it.isNotBlank() }
        .associate { pair ->
            val (k, v) = pair.split(":")
            ProjectTaskId(k) to CalendarDuration(v)
        },
)