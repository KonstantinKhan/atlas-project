package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentDayOverride
import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentId
import com.khan366kos.atlas.project.backend.common.models.resource.Resource
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceCalendarOverride
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceId
import com.khan366kos.atlas.project.backend.common.models.resource.TaskAssignment
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ResourceRepoInMemory : IResourceRepo {
    private val resources = mutableMapOf<String, Resource>()
    private val overrides = mutableListOf<ResourceCalendarOverride>()
    private val assignments = mutableMapOf<String, TaskAssignment>()
    private val assignmentPlanIds = mutableMapOf<String, String>()
    private val dayOverrides = mutableListOf<AssignmentDayOverride>()

    override suspend fun listResources(): List<Resource> =
        resources.values.sortedBy { it.sortOrder }

    override suspend fun getResource(id: String): Resource? = resources[id]

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createResource(resource: Resource): Resource {
        val newId = Uuid.random().toString()
        val maxOrder = resources.values.maxOfOrNull { it.sortOrder } ?: -1
        val created = resource.copy(id = ResourceId(newId), sortOrder = maxOrder + 1)
        resources[newId] = created
        return created
    }

    override suspend fun updateResource(resource: Resource): Resource {
        resources[resource.id.value] = resource
        return resource
    }

    override suspend fun deleteResource(id: String): Int {
        overrides.removeAll { it.resourceId.value == id }
        return if (resources.remove(id) != null) 1 else 0
    }

    override suspend fun getCalendarOverrides(resourceId: String): List<ResourceCalendarOverride> =
        overrides.filter { it.resourceId.value == resourceId }.sortedBy { it.date }

    override suspend fun setCalendarOverride(override: ResourceCalendarOverride): Int {
        overrides.removeAll { it.resourceId == override.resourceId && it.date == override.date }
        overrides.add(override)
        return 1
    }

    override suspend fun deleteCalendarOverride(resourceId: String, date: LocalDate): Int {
        val removed = overrides.removeAll { it.resourceId.value == resourceId && it.date == date }
        return if (removed) 1 else 0
    }

    override suspend fun listAssignments(planId: String): List<TaskAssignment> =
        assignments.filter { assignmentPlanIds[it.key] == planId }.values.toList()

    override suspend fun getAssignmentsByTask(taskId: String): List<TaskAssignment> =
        assignments.values.filter { it.taskId.value == taskId }

    override suspend fun getAssignmentsByResource(resourceId: String): List<TaskAssignment> =
        assignments.values.filter { it.resourceId.value == resourceId }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createAssignment(planId: String, assignment: TaskAssignment): TaskAssignment {
        val newId = Uuid.random().toString()
        val created = assignment.copy(id = AssignmentId(newId))
        assignments[newId] = created
        assignmentPlanIds[newId] = planId
        return created
    }

    override suspend fun updateAssignment(id: String, hoursPerDay: Double, plannedEffortHours: Double?): TaskAssignment {
        val existing = assignments[id] ?: throw NoSuchElementException("Assignment $id not found")
        val updated = existing.copy(hoursPerDay = hoursPerDay, plannedEffortHours = plannedEffortHours)
        assignments[id] = updated
        return updated
    }

    override suspend fun deleteAssignment(id: String): Int {
        assignmentPlanIds.remove(id)
        dayOverrides.removeAll { it.assignmentId.value == id }
        return if (assignments.remove(id) != null) 1 else 0
    }

    override suspend fun getAssignmentDayOverrides(assignmentId: String): List<AssignmentDayOverride> =
        dayOverrides.filter { it.assignmentId.value == assignmentId }.sortedBy { it.date }

    override suspend fun setAssignmentDayOverride(override: AssignmentDayOverride): Int {
        dayOverrides.removeAll { it.assignmentId == override.assignmentId && it.date == override.date }
        dayOverrides.add(override)
        return 1
    }

    override suspend fun deleteAssignmentDayOverride(assignmentId: String, date: LocalDate): Int {
        val removed = dayOverrides.removeAll { it.assignmentId.value == assignmentId && it.date == date }
        return if (removed) 1 else 0
    }

    override suspend fun getAllDayOverridesForPlan(planId: String): List<AssignmentDayOverride> {
        val planAssignmentIds = assignmentPlanIds.filter { it.value == planId }.keys
        return dayOverrides.filter { it.assignmentId.value in planAssignmentIds }
    }
}
