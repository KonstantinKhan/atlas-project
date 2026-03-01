package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlanId
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.enums.ProjectTaskStatus
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.repo.postgres.mapper.toHoliday
import com.khan366kos.atlas.project.backend.repo.postgres.mapper.toTimelineCalendar
import com.khan366kos.atlas.project.backend.repo.postgres.mapper.toWorkingWeekend
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectPlansTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.TaskDependenciesTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.TaskSchedulesTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarHolidaysTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.TimelineCalendarWorkingWeekendsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class AtlasProjectTaskRepoPostgres(private val database: Database) : IAtlasProjectTaskRepo {
    override suspend fun timelineCalendar(): TimelineCalendar = newSuspendedTransaction(db = database) {
        val calendarRow = TimelineCalendarTable.selectAll().single()
        val calendar = calendarRow.toTimelineCalendar()
        val holidays = TimelineCalendarHolidaysTable
            .selectAll()
            .where { TimelineCalendarHolidaysTable.calendarId eq calendarRow[TimelineCalendarTable.id].value }
            .map { it.toHoliday() }
        val workingWeekends = TimelineCalendarWorkingWeekendsTable
            .selectAll()
            .where { TimelineCalendarWorkingWeekendsTable.calendarId eq calendarRow[TimelineCalendarTable.id].value }
            .map { it.toWorkingWeekend() }
        calendar.copy(
            holidays = holidays.toSet(),
            workingWeekends = workingWeekends.toSet()
        )
    }

    override suspend fun projectPlan(): ProjectPlan = newSuspendedTransaction(db = database) {
        // TODO: поддержка нескольких планов — принимать planId параметром вместо .single()
        val planUuid = ProjectPlansTable.selectAll().single()[ProjectPlansTable.id]
        val planIdStr = planUuid.toString()

        val tasks = ProjectTasksTable.selectAll()
            .where { ProjectTasksTable.projectPlanId eq planUuid }
            .map { it.toProjectTask() }

        val taskUuids = tasks.map { UUID.fromString(it.id.value) }

        val schedules: MutableMap<TaskScheduleId, TaskSchedule> = TaskSchedulesTable.selectAll()
            .where { TaskSchedulesTable.taskId inList taskUuids }
            .associate { row ->
                val id = TaskScheduleId(row[TaskSchedulesTable.taskId].toString())
                id to TaskSchedule(
                    id = id,
                    start = ProjectDate.Set(row[TaskSchedulesTable.plannedStart]),
                    end = ProjectDate.Set(row[TaskSchedulesTable.plannedEnd])
                )
            }.toMutableMap()

        val dependencies: MutableSet<TaskDependency> = TaskDependenciesTable.selectAll()
            .where { TaskDependenciesTable.projectPlanId eq planUuid }
            .map { row ->
                TaskDependency(
                    predecessor = TaskId(row[TaskDependenciesTable.predecessorTaskId].toString()),
                    successor = TaskId(row[TaskDependenciesTable.successorTaskId].toString()),
                    type = DependencyType.valueOf(row[TaskDependenciesTable.type]),
                    lag = Duration(row[TaskDependenciesTable.lagDays])
                )
            }.toMutableSet()

        ProjectPlan(
            id = ProjectPlanId(planIdStr),
            tasks = tasks.associateBy { it.id }.toMutableMap(),
            schedules = schedules,
            dependencies = dependencies
        )
    }

    override suspend fun updateSchedule(schedule: TaskSchedule) = newSuspendedTransaction(db = database) {
        TaskSchedulesTable.update({ TaskSchedulesTable.taskId eq UUID.fromString(schedule.id.value) }) { row ->
            row[TaskSchedulesTable.plannedStart] = (schedule.start as ProjectDate.Set).date
            row[TaskSchedulesTable.plannedEnd] = (schedule.end as ProjectDate.Set).date
        }
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
        val planUuid = ProjectPlansTable.selectAll().single()[ProjectPlansTable.id]
        val taskUuid = java.util.UUID.randomUUID()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        ProjectTasksTable.insert {
            it[id] = taskUuid
            it[projectPlanId] = planUuid
            it[title] = task.title.value
            it[description] = task.description.value
            it[durationDays] = task.duration.value.toIntOrNull() ?: 0
            it[status] = task.status.name
        }
        TaskSchedulesTable.insert {
            it[taskId] = taskUuid
            it[plannedStart] = today
            it[plannedEnd] = today
        }
        task.copy(id = TaskId(taskUuid.toString()))
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

    override suspend fun addDependency(predecessorId: String, successorId: String, type: String, lagDays: Int): Int =
        newSuspendedTransaction(db = database) {
            val planUuid = ProjectPlansTable.selectAll().single()[ProjectPlansTable.id]
            TaskDependenciesTable.insert {
                it[TaskDependenciesTable.predecessorTaskId] = UUID.fromString(predecessorId)
                it[TaskDependenciesTable.successorTaskId] = UUID.fromString(successorId)
                it[TaskDependenciesTable.type] = type
                it[TaskDependenciesTable.lagDays] = lagDays
                it[TaskDependenciesTable.projectPlanId] = planUuid
            }
            1
        }
}

private fun ResultRow.toProjectTask() = ProjectTask(
    id = TaskId(this[ProjectTasksTable.id].toString()),
    title = Title(this[ProjectTasksTable.title]),
    description = Description(this[ProjectTasksTable.description]),
    duration = Duration(this[ProjectTasksTable.durationDays]),
    status = ProjectTaskStatus.valueOf(this[ProjectTasksTable.status]),
)
