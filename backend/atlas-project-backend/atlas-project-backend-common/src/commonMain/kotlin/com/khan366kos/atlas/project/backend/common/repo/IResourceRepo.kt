package com.khan366kos.atlas.project.backend.common.repo

import com.khan366kos.atlas.project.backend.common.models.resource.AssignmentDayOverride
import com.khan366kos.atlas.project.backend.common.models.resource.Resource
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceCalendarOverride
import com.khan366kos.atlas.project.backend.common.models.resource.TaskAssignment
import kotlinx.datetime.LocalDate

interface IResourceRepo {
    suspend fun listResources(): List<Resource>
    suspend fun getResource(id: String): Resource?
    suspend fun createResource(resource: Resource): Resource
    suspend fun updateResource(resource: Resource): Resource
    suspend fun deleteResource(id: String): Int
    suspend fun getCalendarOverrides(resourceId: String): List<ResourceCalendarOverride>
    suspend fun setCalendarOverride(override: ResourceCalendarOverride): Int
    suspend fun deleteCalendarOverride(resourceId: String, date: LocalDate): Int

    suspend fun listAssignments(planId: String): List<TaskAssignment>
    suspend fun getAssignmentsByTask(taskId: String): List<TaskAssignment>
    suspend fun getAssignmentsByResource(resourceId: String): List<TaskAssignment>
    suspend fun createAssignment(planId: String, assignment: TaskAssignment): TaskAssignment
    suspend fun updateAssignment(id: String, hoursPerDay: Double, plannedEffortHours: Double?): TaskAssignment
    suspend fun deleteAssignment(id: String): Int

    suspend fun getAssignmentDayOverrides(assignmentId: String): List<AssignmentDayOverride>
    suspend fun setAssignmentDayOverride(override: AssignmentDayOverride): Int
    suspend fun deleteAssignmentDayOverride(assignmentId: String, date: LocalDate): Int
    suspend fun getAllDayOverridesForPlan(planId: String): List<AssignmentDayOverride>
}
