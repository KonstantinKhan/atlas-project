# Transport Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-transport/`  
**Module:** [Backend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

The Transport module contains Data Transfer Objects (DTOs) for API communication. These classes define the shape of data exchanged between the backend and frontend.

## Directory Structure

```
src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/
├── calendar/
│   └── (calendar DTOs)
├── commands/
│   ├── AssignScheduleCommandDto.kt
│   ├── ChangeTaskEndDateCommandDto.kt
│   ├── ChangeTaskStartDateCommandDto.kt
│   ├── CreateDependencyCommandDto.kt
│   ├── CreateTaskInPoolCommandDto.kt
│   └── PlanFromEndCommandDto.kt
├── enums/
├── ganttProjectPlan/
├── plan/
├── simple/
├── timelineCalendar/
├── CreateProjectTaskRequest.kt
├── GanttDependencyDto.kt
├── GanttTaskDto.kt
├── ScheduleDeltaDto.kt
├── ScheduledTaskDto.kt
├── TaskDto.kt
├── UpdateProjectTaskRequest.kt
└── WorkCalendar.kt
```

---

## Task DTOs

### TaskDto

**Path:** `TaskDto.kt`

**Purpose:** Basic task data for API responses.

```kotlin
data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val status: String,
)
```

---

### GanttTaskDto

**Path:** `GanttTaskDto.kt`

**Purpose:** Task data with schedule information for Gantt chart display.

```kotlin
data class GanttTaskDto(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val status: String,
    val start: String?,      // ISO date string
    val end: String?,        // ISO date string
)
```

---

### ScheduledTaskDto

**Path:** `ScheduledTaskDto.kt`

**Purpose:** Task with scheduled dates.

```kotlin
data class ScheduledTaskDto(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val status: String,
    val startDate: String,
    val endDate: String,
)
```

---

## Request DTOs

### CreateProjectTaskRequest

**Path:** `CreateProjectTaskRequest.kt`

**Purpose:** Request body for creating a new task.

```kotlin
data class CreateProjectTaskRequest(
    val title: String,
    val description: String = "",
    val duration: Int = 1,
) {
    fun toModel(): ProjectTask { ... }
}
```

---

### UpdateProjectTaskRequest

**Path:** `UpdateProjectTaskRequest.kt`

**Purpose:** Request body for updating a task.

```kotlin
data class UpdateProjectTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val duration: Int? = null,
    val status: String? = null,
) {
    fun applyTo(task: ProjectTask): ProjectTask { ... }
}
```

---

## Command DTOs

### ChangeTaskStartDateCommandDto

**Path:** `commands/ChangeTaskStartDateCommandDto.kt`

**Purpose:** Command to change a task's start date.

```kotlin
data class ChangeTaskStartDateCommandDto(
    val taskId: String,
    val newPlannedStart: String,  // ISO date
)
```

---

### ChangeTaskEndDateCommandDto

**Path:** `commands/ChangeTaskEndDateCommandDto.kt`

**Purpose:** Command to change a task's end date.

```kotlin
data class ChangeTaskEndDateCommandDto(
    val taskId: String,
    val newPlannedEnd: String,  // ISO date
)
```

---

### CreateDependencyCommandDto

**Path:** `commands/CreateDependencyCommandDto.kt`

**Purpose:** Command to create a task dependency.

```kotlin
data class CreateDependencyCommandDto(
    val fromTaskId: String,
    val toTaskId: String,
    val type: String,         // FS, SS, FF, SF
    val lagDays: Int? = 0,
)
```

---

### CreateTaskInPoolCommandDto

**Path:** `commands/CreateTaskInPoolCommandDto.kt`

**Purpose:** Command to create a task without schedule.

```kotlin
data class CreateTaskInPoolCommandDto(
    val title: String,
    val description: String = "",
)
```

---

### AssignScheduleCommandDto

**Path:** `commands/AssignScheduleCommandDto.kt`

**Purpose:** Command to assign a schedule to an existing pool task.

```kotlin
data class AssignScheduleCommandDto(
    val start: String,      // ISO date string
    val duration: Int,      // Duration in days
)
```

**Usage:** Sent when dragging a pool task to the timeline grid.

---

### PlanFromEndCommandDto

**Path:** `commands/PlanFromEndCommandDto.kt`

**Purpose:** Command to plan a task backwards from its end date.

```kotlin
data class PlanFromEndCommandDto(
    val taskId: String,
    val newPlannedEnd: String,  // ISO date (deadline)
)
```

**Usage:** Used for deadline-driven scheduling where end date is fixed.

---

## Dependency DTOs

### GanttDependencyDto

**Path:** `GanttDependencyDto.kt`

**Purpose:** Dependency data for Gantt chart display.

```kotlin
data class GanttDependencyDto(
    val fromTaskId: String,
    val toTaskId: String,
    val type: String,
    val lagDays: Int?,
)
```

---

## Schedule DTOs

### ScheduleDeltaDto

**Path:** `ScheduleDeltaDto.kt`

**Purpose:** Represents changes to schedules after task modifications.

```kotlin
data class ScheduleDeltaDto(
    val updatedSchedule: List<ScheduleUpdateDto>,
)

data class ScheduleUpdateDto(
    val id: String,
    val start: String,
    val end: String,
)
```

---

## Calendar DTOs

### WorkCalendar

**Path:** `WorkCalendar.kt`

**Purpose:** Work calendar configuration.

```kotlin
data class WorkCalendar(
    val holidays: List<String>,
    val workingDays: List<Int>,
    // ... calendar methods
)
```

---

### TimelineCalendarDto

**Path:** `timelineCalendar/`

**Purpose:** Calendar data for timeline display.

```kotlin
data class TimelineCalendarDto(
    val holidays: List<String>,
    val workingDays: List<Int>,
    // ... additional properties
)
```

---

## Gantt Project Plan DTOs

### GanttProjectPlanDto

**Path:** `ganttProjectPlan/`

**Purpose:** Complete project plan for Gantt chart.

```kotlin
data class GanttProjectPlanDto(
    val projectId: String,
    val tasks: List<GanttTaskDto>,
    val dependencies: List<GanttDependencyDto>,
)
```

---

## Dependencies

**Imports:**
- `kotlinx.serialization.Serializable` for JSON serialization

**Imported by:**
- Ktor App module (for request/response types)
- Mappers module (for domain conversions)

---

## Related Files

- [Common Module](./common.md)
- [Mappers Module](./mappers.md)
- [Ktor App Module](./ktor-app.md)
- [Frontend Types](../../frontend/details/types.md)
