# Backend Supporting Modules

**Path:** `/backend/atlas-project-backend`
**Last Updated:** 2026-03-22

## Overview

This document covers the supporting backend modules that complete the architecture: Mappers, PostgreSQL Repository, In-Memory Repository, and Calendar Service.

---

## Mappers Module

**Path:** `atlas-project-backend-mappers`

### Purpose

Bidirectional mapping between domain models (common) and transport DTOs (transport).

### Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/mappers/
├── DomainToTransport.kt    # Domain → DTO mapping
└── TransportToDomain.kt    # DTO → Domain mapping
```

### DomainToTransport.kt

```kotlin
package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.transport.GanttTaskDto

fun ProjectTask.toDto(): GanttTaskDto = GanttTaskDto(
    id = this.id.value,
    title = this.title.value,
    description = this.description.value,
    start = null,  // Schedule mapped separately
    end = null,
    status = this.status.toDto()
)

fun ProjectTaskStatus.toDto(): ProjectTaskStatusDto = when (this) {
    ProjectTaskStatus.EMPTY -> ProjectTaskStatusDto.EMPTY
    ProjectTaskStatus.PLANNED -> ProjectTaskStatusDto.PLANNED
    ProjectTaskStatus.IN_PROGRESS -> ProjectTaskStatusDto.IN_PROGRESS
    ProjectTaskStatus.COMPLETED -> ProjectTaskStatusDto.COMPLETED
}
```

### TransportToDomain.kt

```kotlin
package com.khan366kos.atlas.project.backend.mappers

import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.simple.Duration

fun GanttTaskDto.toDomain(): ProjectTask = ProjectTask(
    id = TaskId(this.id),
    title = Title(this.title),
    description = Description(this.description),
    duration = Duration.ZERO,  // Default duration
    status = this.status.toDomain()
)

fun ProjectTaskStatusDto.toDomain(): ProjectTaskStatus = when (this) {
    ProjectTaskStatusDto.EMPTY -> ProjectTaskStatus.EMPTY
    ProjectTaskStatusDto.PLANNED -> ProjectTaskStatus.PLANNED
    ProjectTaskStatusDto.IN_PROGRESS -> ProjectTaskStatus.IN_PROGRESS
    ProjectTaskStatusDto.COMPLETED -> ProjectTaskStatus.COMPLETED
}
```

### Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(projects.atlasProjectBackendCommon)
    implementation(projects.atlasProjectBackendTransport)
    testImplementation(kotlin("test"))
}
```

---

## PostgreSQL Repository Module

**Path:** `atlas-project-backend-postgres`

### Purpose

Production PostgreSQL implementation of the repository interface using JetBrains Exposed ORM.

### Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/
├── AtlasProjectTaskRepoPostgres.kt    # Repository implementation
├── ProjectTasksTable.kt               # Table definitions
└── table/
│   ├── ProjectPlansTable.kt
│   ├── TaskDependenciesTable.kt
│   ├── TaskSchedulesTable.kt
│   ├── TimelineCalendarHolidaysTable.kt
│   ├── TimelineCalendarTable.kt
│   └── TimelineCalendarWorkingWeekendsTable.kt
└── mapper/
    ├── TimelineCalendarMapper.kt
    ├── TimelineCalendarHolidayMapper.kt
    └── TimelineCalendarWorkingWeekendMapper.kt
```

### AtlasProjectTaskRepoPostgres.kt

```kotlin
package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class AtlasProjectTaskRepoPostgres : IAtlasProjectTaskRepo {
    
    override suspend fun getAll(): List<ProjectTask> = transaction {
        ProjectTasksTable.selectAll().map { row ->
            row.toDomain()
        }
    }
    
    override suspend fun getById(id: TaskId): ProjectTask? = transaction {
        ProjectTasksTable.select { ProjectTasksTable.id eq id.value }
            .map { it.toDomain() }
            .singleOrNull()
    }
    
    override suspend fun create(task: ProjectTask): ProjectTask = transaction {
        val id = ProjectTasksTable.insert {
            it[title] = task.title.value
            it[description] = task.description.value
            it[duration] = task.duration.days
            it[status] = task.status.name
        } get ProjectTasksTable.id
        
        task.copy(id = TaskId(id.toString()))
    }
    
    override suspend fun update(task: ProjectTask): ProjectTask = transaction {
        ProjectTasksTable.update({ ProjectTasksTable.id eq task.id.value }) {
            it[title] = task.title.value
            it[description] = task.description.value
            it[duration] = task.duration.days
            it[status] = task.status.name
        }
        task
    }
    
    override suspend fun delete(id: TaskId) = transaction {
        ProjectTasksTable.delete { ProjectTasksTable.id eq id.value }
    }
    
    // Schedule operations
    override suspend fun getSchedule(taskId: ProjectTaskId): TaskSchedule? = transaction {
        TaskSchedulesTable.select { TaskSchedulesTable.taskId eq taskId.value }
            .map { it.toDomain() }
            .singleOrNull()
    }
    
    override suspend fun assignSchedule(schedule: TaskSchedule) = transaction {
        TaskSchedulesTable.insert {
            it[taskId] = schedule.taskId.value
            it[start] = schedule.start.localDate
            it[end] = schedule.end.localDate
        }
    }
    
    // Dependency operations
    override suspend fun getDependencies(): List<TaskDependency> = transaction {
        TaskDependenciesTable.selectAll().map { it.toDomain() }
    }
    
    override suspend fun createDependency(dependency: TaskDependency) = transaction {
        TaskDependenciesTable.insert {
            it[fromTaskId] = dependency.fromTaskId.value
            it[toTaskId] = dependency.toTaskId.value
            it[type] = dependency.type.name
        }
    }
    
    override suspend fun deleteDependency(from: ProjectTaskId, to: ProjectTaskId) = transaction {
        TaskDependenciesTable.delete {
            (TaskDependenciesTable.fromTaskId eq from.value) and
            (TaskDependenciesTable.toTaskId eq to.value)
        }
    }
    
    // Calendar operations
    override suspend fun getTimelineCalendar(): TimelineCalendar = transaction {
        // Load calendar with holidays and working weekends
    }
    
    override suspend fun updateTimelineCalendar(calendar: TimelineCalendar) = transaction {
        // Update calendar configuration
    }
}
```

### Table Definitions

```kotlin
// ProjectTasksTable.kt
object ProjectTasksTable : LongIdTable("project_tasks") {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val duration = integer("duration").default(0)
    val status = enumerationByName("status", 20, ProjectTaskStatus::class)
}

// TaskSchedulesTable.kt
object TaskSchedulesTable : LongIdTable("task_schedules") {
    val taskId = reference("task_id", ProjectTasksTable, onDelete = ReferenceOption.CASCADE)
    val start = date("start")
    val end = date("end")
}

// TaskDependenciesTable.kt
object TaskDependenciesTable : Table("task_dependencies") {
    val fromTaskId = reference("from_task_id", ProjectTasksTable, onDelete = ReferenceOption.CASCADE)
    val toTaskId = reference("to_task_id", ProjectTasksTable, onDelete = ReferenceOption.CASCADE)
    val type = enumerationByName("type", 10, DependencyType::class)
    
    override val primaryKey = PrimaryKey(fromTaskId, toTaskId)
}

// TimelineCalendarTable.kt
object TimelineCalendarTable : LongIdTable("timeline_calendars") {
    val name = varchar("name", 255)
    val workingWeek = text("working_week")  // JSON array
}
```

### Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.atlasProjectBackendCommon)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.postgresql)
    testImplementation(kotlin("test"))
}
```

---

## In-Memory Repository Module

**Path:** `atlas-project-backend-repo-in-memory`

### Purpose

In-memory repository implementation for testing using H2 database.

### Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/repo/inmemory/
├── AtlasProjectTaskRepoInMemory.kt    # In-memory implementation
└── ProjectTasksTable.kt               # Table definitions
```

### AtlasProjectTaskRepoInMemory.kt

```kotlin
package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

class AtlasProjectTaskRepoInMemory : IAtlasProjectTaskRepo {
    
    private val tasks = ConcurrentHashMap<String, ProjectTask>()
    private val schedules = ConcurrentHashMap<String, TaskSchedule>()
    private val dependencies = ConcurrentHashMap<Pair<String, String>, TaskDependency>()
    
    override suspend fun getAll(): List<ProjectTask> = tasks.values.toList()
    
    override suspend fun getById(id: TaskId): ProjectTask? = tasks[id.value]
    
    override suspend fun create(task: ProjectTask): ProjectTask {
        tasks[task.id.value] = task
        return task
    }
    
    override suspend fun update(task: ProjectTask): ProjectTask {
        tasks[task.id.value] = task
        return task
    }
    
    override suspend fun delete(id: TaskId) {
        tasks.remove(id.value)
    }
    
    override suspend fun getSchedule(taskId: ProjectTaskId): TaskSchedule? = 
        schedules[taskId.value]
    
    override suspend fun assignSchedule(schedule: TaskSchedule) {
        schedules[schedule.taskId.value] = schedule
    }
    
    override suspend fun getDependencies(): List<TaskDependency> = 
        dependencies.values.toList()
    
    override suspend fun createDependency(dependency: TaskDependency) {
        dependencies[dependency.fromTaskId.value to dependency.toTaskId.value] = dependency
    }
    
    override suspend fun deleteDependency(from: ProjectTaskId, to: ProjectTaskId) {
        dependencies.remove(from.value to to.value)
    }
    
    // Calendar operations with in-memory storage
    override suspend fun getTimelineCalendar(): TimelineCalendar = 
        defaultCalendar
    
    override suspend fun updateTimelineCalendar(calendar: TimelineCalendar) {
        // Update in-memory calendar
    }
}
```

### Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.atlasProjectBackendCommon)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.h2)
    testImplementation(kotlin("test"))
}
```

---

## Calendar Service Module

**Path:** `atlas-project-backend-calendar-service`

### Purpose

Business logic for working calendar calculations, including working days, holidays, and weekend handling.

### Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/calendar/service/
└── CacheCalendarProvider.kt    # Calendar provider with caching
```

### CacheCalendarProvider.kt

```kotlin
package com.khan366kos.atlas.project.backend.calendar.service

import kotlinx.datetime.*

class CacheCalendarProvider(
    private val calendar: TimelineCalendar
) {
    private val holidayCache = mutableSetOf<LocalDate>()
    private val workingWeekendCache = mutableSetOf<LocalDate>()
    
    init {
        calendar.holidays.forEach { holidayCache.add(it.date) }
        calendar.workingWeekends.forEach { workingWeekendCache.add(it) }
    }
    
    fun isWorkingDay(date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek
        
        // Check if it's a holiday
        if (date in holidayCache) return false
        
        // Check if it's a weekend
        if (dayOfWeek in DayOfWeek.entries.filterNot { it in calendar.workingWeek }) {
            // Unless it's a working weekend
            return date in workingWeekendCache
        }
        
        return true
    }
    
    fun addWorkingDays(startDate: LocalDate, days: Int): LocalDate {
        var result = startDate
        var daysAdded = 0
        
        while (daysAdded < days) {
            result = result.plus(1, DateTimeUnit.DAY)
            if (isWorkingDay(result)) {
                daysAdded++
            }
        }
        
        return result
    }
    
    fun calculateDuration(start: LocalDate, end: LocalDate): Int {
        var days = 0
        var current = start
        
        while (current <= end) {
            if (isWorkingDay(current)) {
                days++
            }
            current = current.plus(1, DateTimeUnit.DAY)
        }
        
        return days
    }
    
    fun getWorkingDaysInRange(start: LocalDate, end: LocalDate): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var current = start
        
        while (current <= end) {
            if (isWorkingDay(current)) {
                result.add(current)
            }
            current = current.plus(1, DateTimeUnit.DAY)
        }
        
        return result
    }
}
```

### Usage Example

```kotlin
val calendar = TimelineCalendar(
    id = "cal-1",
    name = "Standard",
    workingWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                         DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
    holidays = listOf(Holiday(LocalDate(2026, 1, 1), "New Year")),
    workingWeekends = emptyList()
)

val provider = CacheCalendarProvider(calendar)

// Calculate end date: 5 working days from March 15
val endDate = provider.addWorkingDays(LocalDate(2026, 3, 15), 5)

// Check if a date is a working day
val isWorking = provider.isWorkingDay(LocalDate(2026, 3, 20))

// Calculate working days between dates
val duration = provider.calculateDuration(
    LocalDate(2026, 3, 15),
    LocalDate(2026, 3, 25)
)
```

### Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(projects.atlasProjectBackendCommon)
    testImplementation(kotlin("test"))
}
```

---

## Module Dependencies

```
ktor-app
├── transport
├── common
├── project-service
│   └── common
├── mappers
│   ├── transport
│   └── common
├── postgres
│   └── common
├── repo-in-memory
│   └── common
└── calendar-service
    └── common
```

---

## Project Service Module

**Path:** `atlas-project-backend-project-service`

### Purpose

Orchestration layer for project-level operations, providing unified access to project plans and coordinating between repositories.

### Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/project/service/
└── ProjectService.kt    # Project orchestration
```

### ProjectService.kt

```kotlin
package com.khan366kos.atlas.project.backend.project.service

import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlanId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo

class ProjectService(
    private val projectRepo: IAtlasProjectTaskRepo
) {
    suspend fun project(projectId: ProjectPlanId) = 
        projectRepo.projectPlan(projectId.asString())
}
```

### Usage

Injected into Ktor application for route coordination:

```kotlin
// Application.kt
fun Application.module(config: AppConfig) {
    val projectService = ProjectService(config.repo)
    configureRouting(config, projectService)
}

// plugins/Routing.kt
fun Application.configureRouting(
    appConfig: AppConfig,
    projectService: ProjectService
) {
    routing {
        route("/projects/{projectId}") {
            projectPlan(appConfig.repo, ...)
            criticalPath(appConfig.repo, ...)
            analysis(appConfig.repo, ...)
            // ... other routes
        }
    }
}
```

### Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.atlasProjectBackendCommon)
    testImplementation(kotlin("test"))
}
```

### Testing

```kotlin
class ProjectServiceTest {
    private val mockRepo = mockk<IAtlasProjectTaskRepo>()
    private val service = ProjectService(mockRepo)

    @Test
    fun `get project returns project plan`() = runBlocking {
        val projectId = ProjectPlanId("test-1")
        val expectedPlan = ProjectPlan(projectId, emptyList(), emptyList(), emptyList())
        
        coEvery { mockRepo.projectPlan("test-1") } returns expectedPlan
        
        val result = service.project(projectId)
        
        assertEquals(expectedPlan, result)
    }
}
```

---

## Testing Strategy

### Unit Tests

Test individual modules in isolation:

```kotlin
// CalendarServiceTest.kt
class CalendarServiceTest {
    @Test
    fun `isWorkingDay returns false for holidays`() {
        val calendar = TimelineCalendar(
            id = "test",
            name = "Test",
            workingWeek = DayOfWeek.entries.filter { it.ordinal < 5 },
            holidays = listOf(Holiday(LocalDate(2026, 12, 25), "Christmas")),
            workingWeekends = emptyList()
        )
        val provider = CacheCalendarProvider(calendar)
        
        assertFalse(provider.isWorkingDay(LocalDate(2026, 12, 25)))
    }
}
```

### Integration Tests

Use in-memory repository for integration tests:

```kotlin
// RepositoryIntegrationTest.kt
class RepositoryIntegrationTest {
    private val repo = AtlasProjectTaskRepoInMemory()
    
    @Test
    fun `create and get task`() = runBlocking {
        val task = ProjectTask(
            id = TaskId("test-1"),
            title = Title("Test"),
            description = Description("Test task"),
            duration = Duration(5),
            status = ProjectTaskStatus.PLANNED
        )
        
        val created = repo.create(task)
        val retrieved = repo.getById(TaskId("test-1"))
        
        assertEquals(created.id, retrieved?.id)
    }
}
```

## Related Documentation

- [Common Module README](./atlas-project-backend-common/README.md)
- [Transport Module README](./atlas-project-backend-transport/README.md)
- [Ktor Application README](./atlas-project-backend-ktor-app/README.md)
