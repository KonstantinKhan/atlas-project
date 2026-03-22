package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import java.util.UUID

class AtlasProjectTaskRepoInMemory(private val database: Database) : IAtlasProjectTaskRepo {
    override suspend fun timelineCalendar(): TimelineCalendar {
        TODO("Not yet implemented")
    }

    override suspend fun projectPlan(planId: String): ProjectPlan {
        TODO("Not yet implemented")
    }

    override suspend fun updateSchedule(schedule: TaskSchedule): Int {
        TODO("Not yet implemented")
    }

    override suspend fun tasks(): List<ProjectTask> = newSuspendedTransaction(db = database) {
        ProjectTasksTable.selectAll().map { it.toProjectTask() }
    }

    override suspend fun getTask(id: String): ProjectTask? = newSuspendedTransaction(db = database) {
        ProjectTasksTable.selectAll()
            .where { ProjectTasksTable.id eq id }
            .singleOrNull()?.toProjectTask()
    }

    override suspend fun createTask(planId: String, task: ProjectTask): ProjectTask = newSuspendedTransaction(db = database) {
        ProjectTasksTable.insert {
            it[ProjectTasksTable.id] = task.id.value
            it[ProjectTasksTable.title] = task.title.value
            it[ProjectTasksTable.description] = task.description.value
            it[ProjectTasksTable.status] = task.status.name
        }
        task
    }

    override suspend fun createTaskWithoutSchedule(planId: String, task: ProjectTask): ProjectTask = newSuspendedTransaction(db = database) {
        ProjectTasksTable.insert {
            it[ProjectTasksTable.id] = task.id.value
            it[ProjectTasksTable.title] = task.title.value
            it[ProjectTasksTable.description] = task.description.value
            it[ProjectTasksTable.status] = task.status.name
        }
        task
    }

    override suspend fun updateTask(task: ProjectTask): ProjectTask = newSuspendedTransaction(db = database) {
        ProjectTasksTable.update({ ProjectTasksTable.id eq task.id.value }) {
            it[ProjectTasksTable.title] = task.title.value
            it[ProjectTasksTable.description] = task.description.value
            it[ProjectTasksTable.status] = task.status.name
        }
        task
    }

    override suspend fun addDependency(planId: String, predecessorId: String, successorId: String, type: String, lagDays: Int): Int {
        TODO("Not yet implemented - in-memory repo does not support dependencies")
    }

    override suspend fun updateDependencyLag(predecessorId: String, successorId: String, lag: Int): Int {
        TODO("Not yet implemented - in-memory repo does not support dependencies")
    }

    override suspend fun updateDependency(predecessorId: String, successorId: String, type: String, lagDays: Int): Int {
        TODO("Not yet implemented - in-memory repo does not support dependencies")
    }

    override suspend fun deleteDependency(predecessorId: String, successorId: String): Int {
        TODO("Not yet implemented - in-memory repo does not support dependencies")
    }

    override suspend fun deleteTask(id: String): Int = newSuspendedTransaction(db = database) {
        ProjectTasksTable.deleteWhere { ProjectTasksTable.id eq id }
    }

    override suspend fun reorderTasks(orderedIds: List<String>) {
        TODO("Not yet implemented - in-memory repo does not support reordering")
    }

    override suspend fun saveBaseline(planId: String) {
        TODO("Not yet implemented - in-memory repo does not support baselines")
    }
}

private fun ResultRow.toProjectTask() = ProjectTask(
    id = TaskId(this[ProjectTasksTable.id].toString()),
    title = Title(this[ProjectTasksTable.title]),
    description = Description(this[ProjectTasksTable.description]),
    duration = Duration(0),
    status = ProjectTaskStatus.valueOf(this[ProjectTasksTable.status]),
)
