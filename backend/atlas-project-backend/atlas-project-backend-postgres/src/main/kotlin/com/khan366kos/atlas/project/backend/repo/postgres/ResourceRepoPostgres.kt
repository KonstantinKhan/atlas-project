package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentDayOverride
import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentId
import com.khan366kos.atlas.project.backend.common.models.resource.Resource
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceCalendarOverride
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceId
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceName
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceType
import com.khan366kos.atlas.project.backend.common.models.resource.TaskAssignment
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.repo.postgres.table.AssignmentDayOverridesTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ResourceCalendarOverridesTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ResourcesTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.TaskAssignmentsTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class ResourceRepoPostgres(private val database: Database) : IResourceRepo {

    override suspend fun listResources(): List<Resource> = newSuspendedTransaction(db = database) {
        ResourcesTable.selectAll()
            .orderBy(ResourcesTable.sortOrder)
            .map { it.toResource() }
    }

    override suspend fun getResource(id: String): Resource? = newSuspendedTransaction(db = database) {
        ResourcesTable.selectAll()
            .where { ResourcesTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toResource()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createResource(resource: Resource): Resource = newSuspendedTransaction(db = database) {
        val newId = Uuid.random().toJavaUuid()
        val maxOrder = ResourcesTable.selectAll()
            .maxOfOrNull { it[ResourcesTable.sortOrder] } ?: -1

        ResourcesTable.insert {
            it[id] = newId
            it[name] = resource.name.value
            it[type] = resource.type.name
            it[capacityHoursPerDay] = resource.capacityHoursPerDay.toBigDecimal()
            it[sortOrder] = maxOrder + 1
        }

        resource.copy(
            id = ResourceId(newId.toString()),
            sortOrder = maxOrder + 1,
        )
    }

    override suspend fun updateResource(resource: Resource): Resource = newSuspendedTransaction(db = database) {
        ResourcesTable.update({ ResourcesTable.id eq UUID.fromString(resource.id.value) }) {
            it[name] = resource.name.value
            it[type] = resource.type.name
            it[capacityHoursPerDay] = resource.capacityHoursPerDay.toBigDecimal()
        }
        resource
    }

    override suspend fun deleteResource(id: String): Int = newSuspendedTransaction(db = database) {
        ResourceCalendarOverridesTable.deleteWhere {
            resourceId eq UUID.fromString(id)
        }
        ResourcesTable.deleteWhere { ResourcesTable.id eq UUID.fromString(id) }
    }

    override suspend fun getCalendarOverrides(resourceId: String): List<ResourceCalendarOverride> =
        newSuspendedTransaction(db = database) {
            ResourceCalendarOverridesTable.selectAll()
                .where { ResourceCalendarOverridesTable.resourceId eq UUID.fromString(resourceId) }
                .orderBy(ResourceCalendarOverridesTable.overrideDate)
                .map { row ->
                    ResourceCalendarOverride(
                        resourceId = ResourceId(resourceId),
                        date = row[ResourceCalendarOverridesTable.overrideDate],
                        availableHours = row[ResourceCalendarOverridesTable.availableHours].toDouble(),
                    )
                }
        }

    override suspend fun setCalendarOverride(override: ResourceCalendarOverride): Int =
        newSuspendedTransaction(db = database) {
            val resUuid = UUID.fromString(override.resourceId.value)
            val existing = ResourceCalendarOverridesTable.selectAll()
                .where {
                    (ResourceCalendarOverridesTable.resourceId eq resUuid) and
                            (ResourceCalendarOverridesTable.overrideDate eq override.date)
                }
                .singleOrNull()

            if (existing != null) {
                ResourceCalendarOverridesTable.update({
                    (ResourceCalendarOverridesTable.resourceId eq resUuid) and
                            (ResourceCalendarOverridesTable.overrideDate eq override.date)
                }) {
                    it[availableHours] = override.availableHours.toBigDecimal()
                }
            } else {
                ResourceCalendarOverridesTable.insert {
                    it[resourceId] = resUuid
                    it[overrideDate] = override.date
                    it[availableHours] = override.availableHours.toBigDecimal()
                }
                1
            }
        }

    override suspend fun deleteCalendarOverride(resourceId: String, date: LocalDate): Int =
        newSuspendedTransaction(db = database) {
            ResourceCalendarOverridesTable.deleteWhere {
                (ResourceCalendarOverridesTable.resourceId eq UUID.fromString(resourceId)) and
                        (overrideDate eq date)
            }
        }

    override suspend fun listAssignments(planId: String): List<TaskAssignment> =
        newSuspendedTransaction(db = database) {
            TaskAssignmentsTable.selectAll()
                .where { TaskAssignmentsTable.projectPlanId eq UUID.fromString(planId) }
                .map { it.toAssignment() }
        }

    override suspend fun getAssignmentsByTask(taskId: String): List<TaskAssignment> =
        newSuspendedTransaction(db = database) {
            TaskAssignmentsTable.selectAll()
                .where { TaskAssignmentsTable.taskId eq UUID.fromString(taskId) }
                .map { it.toAssignment() }
        }

    override suspend fun getAssignmentsByResource(resourceId: String): List<TaskAssignment> =
        newSuspendedTransaction(db = database) {
            TaskAssignmentsTable.selectAll()
                .where { TaskAssignmentsTable.resourceId eq UUID.fromString(resourceId) }
                .map { it.toAssignment() }
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createAssignment(planId: String, assignment: TaskAssignment): TaskAssignment =
        newSuspendedTransaction(db = database) {
            val newId = Uuid.random().toJavaUuid()
            TaskAssignmentsTable.insert {
                it[id] = newId
                it[projectPlanId] = UUID.fromString(planId)
                it[taskId] = UUID.fromString(assignment.taskId.value)
                it[resourceId] = UUID.fromString(assignment.resourceId.value)
                it[hoursPerDay] = assignment.hoursPerDay.toBigDecimal()
                it[plannedEffortHours] = assignment.plannedEffortHours?.toBigDecimal()
            }
            assignment.copy(id = AssignmentId(newId.toString()))
        }

    override suspend fun updateAssignment(id: String, hoursPerDay: Double, plannedEffortHours: Double?): TaskAssignment =
        newSuspendedTransaction(db = database) {
            TaskAssignmentsTable.update({ TaskAssignmentsTable.id eq UUID.fromString(id) }) {
                it[TaskAssignmentsTable.hoursPerDay] = hoursPerDay.toBigDecimal()
                it[TaskAssignmentsTable.plannedEffortHours] = plannedEffortHours?.toBigDecimal()
            }
            TaskAssignmentsTable.selectAll()
                .where { TaskAssignmentsTable.id eq UUID.fromString(id) }
                .single()
                .toAssignment()
        }

    override suspend fun deleteAssignment(id: String): Int = newSuspendedTransaction(db = database) {
        TaskAssignmentsTable.deleteWhere { TaskAssignmentsTable.id eq UUID.fromString(id) }
    }

    private fun ResultRow.toResource() = Resource(
        id = ResourceId(this[ResourcesTable.id].toString()),
        name = ResourceName(this[ResourcesTable.name]),
        type = ResourceType.valueOf(this[ResourcesTable.type]),
        capacityHoursPerDay = this[ResourcesTable.capacityHoursPerDay].toDouble(),
        sortOrder = this[ResourcesTable.sortOrder],
    )

    private fun ResultRow.toAssignment() = TaskAssignment(
        id = AssignmentId(this[TaskAssignmentsTable.id].toString()),
        taskId = TaskId(this[TaskAssignmentsTable.taskId].toString()),
        resourceId = ResourceId(this[TaskAssignmentsTable.resourceId].toString()),
        hoursPerDay = this[TaskAssignmentsTable.hoursPerDay].toDouble(),
        plannedEffortHours = this[TaskAssignmentsTable.plannedEffortHours]?.toDouble(),
    )

    override suspend fun getAssignmentDayOverrides(assignmentId: String): List<AssignmentDayOverride> =
        newSuspendedTransaction(db = database) {
            AssignmentDayOverridesTable.selectAll()
                .where { AssignmentDayOverridesTable.assignmentId eq UUID.fromString(assignmentId) }
                .orderBy(AssignmentDayOverridesTable.overrideDate)
                .map { row ->
                    AssignmentDayOverride(
                        assignmentId = AssignmentId(assignmentId),
                        date = row[AssignmentDayOverridesTable.overrideDate],
                        hours = row[AssignmentDayOverridesTable.hours].toDouble(),
                    )
                }
        }

    override suspend fun setAssignmentDayOverride(override: AssignmentDayOverride): Int =
        newSuspendedTransaction(db = database) {
            val aUuid = UUID.fromString(override.assignmentId.value)
            val existing = AssignmentDayOverridesTable.selectAll()
                .where {
                    (AssignmentDayOverridesTable.assignmentId eq aUuid) and
                            (AssignmentDayOverridesTable.overrideDate eq override.date)
                }
                .singleOrNull()

            if (existing != null) {
                AssignmentDayOverridesTable.update({
                    (AssignmentDayOverridesTable.assignmentId eq aUuid) and
                            (AssignmentDayOverridesTable.overrideDate eq override.date)
                }) {
                    it[hours] = override.hours.toBigDecimal()
                }
            } else {
                AssignmentDayOverridesTable.insert {
                    it[assignmentId] = aUuid
                    it[overrideDate] = override.date
                    it[hours] = override.hours.toBigDecimal()
                }
                1
            }
        }

    override suspend fun deleteAssignmentDayOverride(assignmentId: String, date: LocalDate): Int =
        newSuspendedTransaction(db = database) {
            AssignmentDayOverridesTable.deleteWhere {
                (AssignmentDayOverridesTable.assignmentId eq UUID.fromString(assignmentId)) and
                        (overrideDate eq date)
            }
        }

    override suspend fun getAllDayOverridesForPlan(planId: String): List<AssignmentDayOverride> =
        newSuspendedTransaction(db = database) {
            val assignmentIds = TaskAssignmentsTable.selectAll()
                .where { TaskAssignmentsTable.projectPlanId eq UUID.fromString(planId) }
                .map { it[TaskAssignmentsTable.id] }
            if (assignmentIds.isEmpty()) return@newSuspendedTransaction emptyList()
            AssignmentDayOverridesTable.selectAll()
                .where { AssignmentDayOverridesTable.assignmentId inList assignmentIds }
                .map { row ->
                    AssignmentDayOverride(
                        assignmentId = AssignmentId(row[AssignmentDayOverridesTable.assignmentId].toString()),
                        date = row[AssignmentDayOverridesTable.overrideDate],
                        hours = row[AssignmentDayOverridesTable.hours].toDouble(),
                    )
                }
        }
}
