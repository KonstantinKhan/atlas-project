package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.repo.postgres.ProjectTasksTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class AtlasProjectTaskRepoInMemory(private val database: Database) : IAtlasProjectTaskRepo {
    override suspend fun timelineCalendar(): TimelineCalendar {
        TODO("Not yet implemented")
    }

    override suspend fun projectPlan(): ProjectPlan {
        TODO("Not yet implemented")
    }

    override suspend fun tasks(): List<ProjectTask> = newSuspendedTransaction(db = database) {
        ProjectTasksTable.selectAll().map { it.toProjectTask() }
    }

    override suspend fun getTask(id: String): ProjectTask? = newSuspendedTransaction(db = database) {
        ProjectTasksTable.selectAll()
            .where { ProjectTasksTable.id eq UUID.fromString(id) }
            .singleOrNull()?.toProjectTask()
    }

    override suspend fun createTask(task: ProjectTask): ProjectTask = newSuspendedTransaction(db = database) {
        ProjectTasksTable.insert {
            it[id] = UUID.fromString(task.id.value)
            it[projectPlanId] = UUID(0, 0)
            it[title] = task.title.value
            it[description] = task.description.value
            it[durationDays] = task.duration.value.toIntOrNull() ?: 0
            it[status] = task.status.name
        }
        task
    }

    override suspend fun updateTask(task: ProjectTask): ProjectTask = newSuspendedTransaction(db = database) {
        ProjectTasksTable.update({ ProjectTasksTable.id eq UUID.fromString(task.id.value) }) {
            it[title] = task.title.value
            it[description] = task.description.value
            it[durationDays] = task.duration.value.toIntOrNull() ?: 0
            it[status] = task.status.name
        }
        task
    }
}

private fun ResultRow.toProjectTask() = ProjectTask(
    id = TaskId(this[ProjectTasksTable.id].toString()),
    title = Title(this[ProjectTasksTable.title]),
    description = Description(this[ProjectTasksTable.description]),
    duration = Duration(this[ProjectTasksTable.durationDays]),
    status = ProjectTaskStatus.valueOf(this[ProjectTasksTable.status]),
)
