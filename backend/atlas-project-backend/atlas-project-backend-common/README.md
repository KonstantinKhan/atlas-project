# Backend Common Module

**Path:** `/backend/atlas-project-backend/atlas-project-backend-common`  
**Last Updated:** 2026-03-09

## Overview

The common module contains domain models, enums, repository interfaces, and core business logic. This is a **Kotlin Multiplatform** module shared across all other backend modules.

## Purpose

- **Domain Models**: Pure business objects without framework dependencies
- **Repository Interfaces**: Contracts for data access implementations
- **Enums**: Shared enumeration types
- **Business Logic**: Critical Path Method (CPM) algorithm, topological sort
- **Value Objects**: Type-safe wrappers for primitives

## Structure

```
src/commonMain/kotlin/com/khan366kos/atlas/project/backend/common/
├── models/
│   ├── task/
│   │   ├── ProjectTask.kt                # Main task entity
│   │   └── simple/
│   │       ├── TaskId.kt                 # Task ID value object
│   │       ├── Title.kt                  # Title value object
│   │       └── Description.kt            # Description value object
│   ├── taskSchedule/
│   │   ├── TaskSchedule.kt               # Scheduled task
│   │   ├── TaskScheduleId.kt             # Schedule ID
│   │   └── ScheduleDelta.kt              # Schedule change result
│   ├── projectPlan/
│   │   ├── ProjectPlan.kt                # Complete project plan
│   │   ├── ProjectPlanId.kt              # Plan ID
│   │   ├── ConstraintCalculation.kt      # Constraint logic
│   │   ├── TopologicalSort.kt            # Topological sorting
│   │   └── CriticalPathAnalysis.kt       # CPM algorithm
│   └── timelineCalendar/
│       ├── TimelineCalendar.kt           # Calendar definition
│       └── DayOfWeek.kt                  # Day of week enum
├── enums/
│   ├── DependencyType.kt                 # FS, SS, FF, SF
│   └── ProjectTaskStatus.kt              # Task status
├── repo/
│   └── IAtlasProjectTaskRepo.kt          # Repository interface
├── simple/
│   ├── Id.kt                             # Generic ID type
│   └── Duration.kt                       # Duration value object
└── models/
    ├── TaskDependency.kt                 # Task dependency
    └── ProjectDate.kt                    # Date wrapper
```

## Domain Models

### ProjectTask

```kotlin
data class ProjectTask(
    val id: TaskId = TaskId.NONE,
    val title: Title = Title.NONE,
    val description: Description = Description.NONE,
    val duration: Duration = Duration.ZERO,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
)
```

**Responsibility**: Represents a project task with identity and attributes.

### TaskSchedule

```kotlin
data class TaskSchedule(
    val id: TaskScheduleId,
    val taskId: ProjectTaskId,
    val start: ProjectDate,
    val end: ProjectDate
)
```

**Responsibility**: Represents a scheduled task with start and end dates.

### TaskDependency

```kotlin
data class TaskDependency(
    val fromTaskId: ProjectTaskId,
    val toTaskId: ProjectTaskId,
    val type: DependencyType
)
```

**Responsibility**: Represents a dependency relationship between tasks.

### TimelineCalendar

```kotlin
data class TimelineCalendar(
    val id: String,
    val name: String,
    val workingWeek: List<DayOfWeek>,
    val holidays: List<Holiday>,
    val workingWeekends: List<LocalDate>
)
```

**Responsibility**: Defines working days, holidays, and exceptions for scheduling.

### ScheduleDelta

```kotlin
data class ScheduleDelta(
    val updatedSchedules: List<UpdatedSchedule>
)

data class UpdatedSchedule(
    val taskId: String,
    val start: String,
    val end: String
)
```

**Responsibility**: Represents changes made to task schedules after an operation.

## Enums

### DependencyType

```kotlin
enum class DependencyType {
    FS,  // Finish-to-Start: Task B starts after Task A finishes
    SS,  // Start-to-Start: Task B starts after Task A starts
    FF,  // Finish-to-Finish: Task B finishes after Task A finishes
    SF   // Start-to-Finish: Task B finishes after Task A starts
}
```

### ProjectTaskStatus

```kotlin
enum class ProjectTaskStatus {
    EMPTY,       // No status assigned
    PLANNED,     // Task is planned but not started
    IN_PROGRESS, // Task is being worked on
    COMPLETED    // Task is finished
}
```

## Repository Interface

### IAtlasProjectTaskRepo

```kotlin
interface IAtlasProjectTaskRepo {
    // Task operations
    suspend fun getAll(): List<ProjectTask>
    suspend fun getById(id: TaskId): ProjectTask?
    suspend fun create(task: ProjectTask): ProjectTask
    suspend fun update(task: ProjectTask): ProjectTask
    suspend fun delete(id: TaskId)
    
    // Schedule operations
    suspend fun getSchedule(taskId: ProjectTaskId): TaskSchedule?
    suspend fun assignSchedule(schedule: TaskSchedule)
    suspend fun removeSchedule(taskId: ProjectTaskId)
    
    // Dependency operations
    suspend fun getDependencies(): List<TaskDependency>
    suspend fun createDependency(dependency: TaskDependency)
    suspend fun deleteDependency(from: ProjectTaskId, to: ProjectTaskId)
    suspend fun updateDependencyType(from: ProjectTaskId, to: ProjectTaskId, type: DependencyType)
    
    // Calendar operations
    suspend fun getTimelineCalendar(): TimelineCalendar
    suspend fun updateTimelineCalendar(calendar: TimelineCalendar)
    
    // Plan operations
    suspend fun getProjectPlan(): ProjectPlan
}
```

**Implementations**:
- `AtlasProjectTaskRepoPostgres` - PostgreSQL implementation
- `AtlasProjectTaskRepoInMemory` - In-memory implementation for testing

## Business Logic

### Critical Path Method (CPM)

Located in `CriticalPathAnalysis.kt`:

```kotlin
object CriticalPathAnalysis {
    fun calculate(
        tasks: List<ProjectTask>,
        schedules: List<TaskSchedule>,
        dependencies: List<TaskDependency>
    ): CriticalPath {
        // 1. Build dependency graph
        // 2. Topological sort
        // 3. Forward pass - calculate early start/finish
        // 4. Backward pass - calculate late start/finish
        // 5. Calculate slack for each task
        // 6. Identify critical path (tasks with zero slack)
    }
}
```

**Output**:
```kotlin
data class CriticalPath(
    val criticalTaskIds: List<String>,
    val tasks: List<CpmTaskData>
)

data class CpmTaskData(
    val taskId: String,
    val slack: Int  // Days of slack
)
```

### Topological Sort

Located in `TopologicalSort.kt`:

```kotlin
object TopologicalSort {
    fun sort(
        tasks: List<ProjectTask>,
        dependencies: List<TaskDependency>
    ): List<ProjectTask> {
        // Kahn's algorithm or DFS-based topological sort
        // Returns tasks in dependency order
        // Throws exception if cycle detected
    }
}
```

### Constraint Calculation

Located in `ConstraintCalculation.kt`:

```kotlin
object ConstraintCalculation {
    fun calculateConstraints(
        task: ProjectTask,
        dependencies: List<TaskDependency>,
        schedules: List<TaskSchedule>
    ): ConstraintResult {
        // Calculate earliest possible start
        // Calculate latest possible end
        // Apply dependency constraints
    }
}
```

## Value Objects

### TaskId

```kotlin
@JvmInline
value class TaskId(val value: String) {
    companion object {
        val NONE = TaskId("")
    }
}
```

### Duration

```kotlin
@JvmInline
value class Duration(val days: Int) {
    companion object {
        val ZERO = Duration(0)
    }
    
    operator fun plus(other: Duration): Duration = Duration(days + other.days)
}
```

### Title & Description

```kotlin
@JvmInline
value class Title(val value: String) {
    companion object {
        val NONE = Title("")
    }
}

@JvmInline
value class Description(val value: String) {
    companion object {
        val NONE = Description("")
    }
}
```

## Testing

Tests are located in `src/commonTest/kotlin/`:

```kotlin
// ProjectPlanTest.kt
class ProjectPlanTest {
    @Test
    fun `calculate critical path`() {
        // Test CPM algorithm
    }
    
    @Test
    fun `topological sort detects cycle`() {
        // Test cycle detection
    }
}

// TaskScheduleTest.kt
class TaskScheduleTest {
    @Test
    fun `schedule delta calculation`() {
        // Test schedule changes
    }
}

// TimelineCalendarTest.kt
class TimelineCalendarTest {
    @Test
    fun `working days calculation`() {
        // Test calendar logic
    }
}
```

Run tests:
```bash
./gradlew :atlas-project-backend-common:test
```

## Dependencies

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
```

## Usage Examples

### Creating a Task

```kotlin
val task = ProjectTask(
    id = TaskId("task-123"),
    title = Title("Design Phase"),
    description = Description("Complete system design"),
    duration = Duration(5),
    status = ProjectTaskStatus.PLANNED
)
```

### Creating a Dependency

```kotlin
val dependency = TaskDependency(
    fromTaskId = ProjectTaskId("task-1"),
    toTaskId = ProjectTaskId("task-2"),
    type = DependencyType.FS
)
```

### Calculating Critical Path

```kotlin
val criticalPath = CriticalPathAnalysis.calculate(
    tasks = tasks,
    schedules = schedules,
    dependencies = dependencies
)

println("Critical tasks: ${criticalPath.criticalTaskIds}")
println("Task slack: ${criticalPath.tasks.map { it.taskId to it.slack }}")
```

## Related Modules

- **transport**: DTOs for API serialization
- **mappers**: Domain ↔ Transport mapping
- **postgres**: Repository implementation
- **repo-in-memory**: Test repository implementation
- **ktor-app**: Application layer using domain models
