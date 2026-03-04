# Common Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-common/`  
**Module:** [Backend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

The Common module contains domain models and repository interfaces. It defines the core business entities and contracts for data access, independent of any infrastructure concerns.

## Directory Structure

```
src/commonMain/kotlin/com/khan366kos/atlas/project/backend/common/
├── models/
│   ├── projectPlan/
│   │   ├── ProjectPlan.kt
│   │   └── ProjectPlanId.kt
│   ├── task/
│   │   ├── enums/
│   │   ├── simple/
│   │   └── ProjectTask.kt
│   ├── taskSchedule/
│   │   ├── TaskSchedule.kt
│   │   └── TaskScheduleId.kt
│   ├── timelineCalendar/
│   ├── simple/
│   ├── ProjectDate.kt
│   └── TaskDependency.kt
├── repo/
│   └── IAtlasProjectTaskRepo.kt
├── enums/
└── simple/
```

---

## Domain Models

### ProjectTask

**Path:** `models/task/ProjectTask.kt`

**Purpose:** Represents a task in the project plan.

```kotlin
data class ProjectTask(
    val id: TaskId = TaskId.NONE,
    val title: Title = Title.NONE,
    val description: Description = Description.NONE,
    val duration: Duration = Duration.ZERO,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
)
```

**Properties:**
- `id: TaskId` - Unique identifier
- `title: Title` - Task title
- `description: Description` - Task description
- `duration: Duration` - Task duration
- `status: ProjectTaskStatus` - Current status

---

### TaskSchedule

**Path:** `models/taskSchedule/TaskSchedule.kt`

**Purpose:** Represents the scheduled dates for a task.

```kotlin
data class TaskSchedule(
    val id: TaskScheduleId,
    val start: ProjectDate,
    val end: ProjectDate,
)
```

**Properties:**
- `id: TaskScheduleId` - Schedule identifier (matches task ID)
- `start: ProjectDate` - Scheduled start date
- `end: ProjectDate` - Scheduled end date

---

### TaskDependency

**Path:** `models/TaskDependency.kt`

**Purpose:** Represents a dependency between two tasks.

```kotlin
data class TaskDependency(
    val predecessor: TaskId,
    val successor: TaskId,
    val type: DependencyType,
    val lag: Duration,
)
```

**Properties:**
- `predecessor: TaskId` - Predecessor task ID
- `successor: TaskId` - Successor task ID
- `type: DependencyType` - Type of dependency (FS, SS, FF, SF)
- `lag: Duration` - Lag time between tasks

---

### ProjectPlan

**Path:** `models/projectPlan/ProjectPlan.kt`

**Purpose:** Represents the complete project plan with tasks, schedules, and dependencies.

```kotlin
data class ProjectPlan(
    val id: ProjectPlanId,
    private val tasks: MutableMap<TaskId, ProjectTask>,
    private val schedules: MutableMap<TaskScheduleId, TaskSchedule>,
    private val dependencies: MutableList<TaskDependency>,
) {
    fun tasks(): List<ProjectTask> = tasks.values.toList()
    fun schedules(): Map<TaskScheduleId, TaskSchedule> = schedules.toMap()
    fun dependencies(): List<TaskDependency> = dependencies.toList()
    
    fun changeTaskStartDate(...): ScheduleDelta { ... }
    fun changeTaskEndDate(...): ScheduleDelta { ... }
    fun addDependency(...): ScheduleDelta { ... }
}
```

**Key Methods:**
- `changeTaskStartDate()` - Update task start and recalculate
- `changeTaskEndDate()` - Update task end and recalculate
- `addDependency()` - Add dependency and recalculate schedules

---

### ProjectDate

**Path:** `models/ProjectDate.kt`

**Purpose:** Represents a date that can be set or unset.

```kotlin
sealed class ProjectDate {
    object Unset : ProjectDate()
    data class Set(val date: LocalDate) : ProjectDate()
}
```

**Use Case:** Distinguishes between "no date scheduled" and "a specific date".

---

### Duration

**Path:** `models/simple/Duration.kt`

**Purpose:** Value object representing a duration in days.

```kotlin
@JvmInline
value class Duration(private val days: Int) {
    companion object {
        val ZERO = Duration(0)
        val NONE = Duration(-1)
    }
    
    fun asInt(): Int = days
}
```

---

### TaskId, Title, Description

**Path:** `models/task/simple/`

**Purpose:** Value objects for type safety.

```kotlin
@JvmInline
value class TaskId(val value: String) {
    companion object {
        val NONE = TaskId("")
    }
}

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

---

## Repository Interface

### IAtlasProjectTaskRepo

**Path:** `repo/IAtlasProjectTaskRepo.kt`

**Purpose:** Defines the contract for task repository operations.

```kotlin
interface IAtlasProjectTaskRepo {
    // Task operations
    fun createTask(task: ProjectTask): ProjectTask
    fun createTaskWithoutSchedule(task: ProjectTask): ProjectTask
    fun getTask(id: String): ProjectTask?
    fun updateTask(task: ProjectTask): ProjectTask
    
    // Schedule operations
    fun updateSchedule(schedule: TaskSchedule)
    
    // Dependency operations
    fun addDependency(predecessorId: String, successorId: String, 
                      type: String, lagDays: Int)
    
    // Project plan
    fun projectPlan(): ProjectPlan
}
```

**Implementations:**
- `PostgresRepo` - PostgreSQL implementation
- `InMemoryRepo` - In-memory implementation for testing

---

## Enums

### ProjectTaskStatus

**Path:** `models/task/enums/`

```kotlin
enum class ProjectTaskStatus {
    EMPTY,
    IN_PROGRESS,
    DONE
}
```

---

### DependencyType

**Path:** `models/task/enums/` (or similar)

```kotlin
enum class DependencyType {
    FS,  // Finish-to-Start
    SS,  // Start-to-Start
    FF,  // Finish-to-Finish
    SF   // Start-to-Finish
}
```

---

## Dependencies

**Imports:**
- `kotlinx.datetime.LocalDate` for date handling

**Imported by:**
- All other backend modules
- Transport module (for DTO mapping)
- Mappers module (for conversions)
- Repository implementations

---

## Related Files

- [Transport Module](./transport.md)
- [Mappers Module](./mappers.md)
- [Postgres Module](./postgres.md)
- [Repo In-Memory](./repo-in-memory.md)
