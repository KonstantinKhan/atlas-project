# Mappers Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-mappers/`  
**Module:** [Backend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

The Mappers module provides conversion functions between domain models and transport DTOs. It ensures clean separation between internal business logic and external API contracts.

## Files Overview

| File | Purpose |
|------|---------|
| `DomainToTransport.kt` | Domain → DTO conversions |
| `TransportToDomain.kt` | DTO → Domain conversions |

---

## DomainToTransport.kt

### Purpose

Extension functions and utilities for converting domain models to transport DTOs.

### Key Conversions

#### ProjectTask → TaskDto

```kotlin
fun ProjectTask.toDto(): TaskDto = TaskDto(
    id = this.id.value,
    title = this.title.value,
    description = this.description.value,
    duration = this.duration.asInt(),
    status = this.status.name,
)
```

---

#### ProjectTask → ScheduledTaskDto

```kotlin
fun ProjectTask.toScheduledTaskDto(startDate: LocalDate, endDate: LocalDate): ScheduledTaskDto = 
    ScheduledTaskDto(
        id = this.id.value,
        title = this.title.value,
        description = this.description.value,
        duration = this.duration.asInt(),
        status = this.status.name,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
    )
```

---

#### ProjectTask → GanttTaskDto

```kotlin
fun ProjectTask.toGanttDto(schedule: TaskSchedule?): GanttTaskDto =
    GanttTaskDto(
        id = this.id.value,
        title = this.title.value,
        start = (schedule?.start as? ProjectDate.Set)?.date,
        end = (schedule?.end as? ProjectDate.Set)?.date,
        status = ProjectTaskStatus.valueOf(this.status.name),
    )
```

**Note:** `GanttTaskDto` contains only id, title, start, end, and status (no description or duration fields).

---

#### ProjectPlan → GanttProjectPlanDto

```kotlin
fun ProjectPlan.toGanttDto(): GanttProjectPlanDto = 
    GanttProjectPlanDto(
        projectId = this.id.value,
        tasks = this.tasks().map { task ->
            val schedule = this.schedules()[TaskScheduleId(task.id.value)]
            task.toGanttDto(schedule)
        },
        dependencies = this.dependencies().map { it.toDto() },
    )
```

---

#### TaskDependency → GanttDependencyDto

```kotlin
fun TaskDependency.toDto(): GanttDependencyDto = 
    GanttDependencyDto(
        fromTaskId = this.predecessor.value,
        toTaskId = this.successor.value,
        type = this.type.name,
        lagDays = this.lag.asInt(),
    )
```

---

#### ScheduleDelta → ScheduleDeltaDto

```kotlin
fun ScheduleDelta.toDto(): ScheduleDeltaDto =
    ScheduleDeltaDto(
        updatedSchedules = this.updatedSchedule.map { schedule ->
            ScheduleUpdateDto(
                taskId = schedule.id.value,
                start = (schedule.start as ProjectDate.Set).date,
                end = (schedule.end as ProjectDate.Set).date,
            )
        },
    )
```

**Note:** Property is `updatedSchedules` (plural) with `taskId` field in `ScheduleUpdateDto`.

---

#### TimelineCalendar → TimelineCalendarDto

```kotlin
fun TimelineCalendar.toTransport(): TimelineCalendarDto =
    TimelineCalendarDto(
        workingWeekDays = this.workingWeekDays,
        weekendWeekDays = this.weekendWeekDays,
        holidays = this.holidays,
        workingWeekends = this.workingWeekends,
    )
```

**Note:** `TimelineCalendarDto` has `workingWeekDays`, `weekendWeekDays`, `holidays`, and `workingWeekends` properties.

---

## TransportToDomain.kt

### Purpose

Extension functions and utilities for converting transport DTOs to domain models.

### Key Conversions

#### CreateProjectTaskRequest → ProjectTask

```kotlin
fun CreateProjectTaskRequest.toModel(): ProjectTask =
    ProjectTask(
        id = TaskId.NONE,
        title = Title(this.title),
        description = Description(this.description),
        duration = Duration(this.duration),
        status = ProjectTaskStatus.EMPTY,
    )
```

---

#### CreateTaskInPoolCommandDto → ProjectTask

```kotlin
fun CreateTaskInPoolCommandDto.toModel(): ProjectTask =
    ProjectTask(
        id = TaskId.NONE,
        title = Title(this.title),
        description = Description(this.description),
        duration = Duration.ZERO,
        status = ProjectTaskStatus.EMPTY,
    )
```

---

#### DependencyTypeDto → Domain

```kotlin
fun DependencyTypeDto.toDomain(): DependencyType =
    DependencyType.valueOf(this.name)
```

---

#### UpdateProjectTaskRequest → Domain Update

```kotlin
fun ProjectTask.applyUpdate(request: UpdateProjectTaskRequest): ProjectTask =
    this.copy(
        title = request.title?.let { Title(it) } ?: this.title,
        description = request.description?.let { Description(it) } ?: this.description,
        duration = request.duration?.let { Duration(it) } ?: this.duration,
        status = request.status?.let { ProjectTaskStatus.valueOf(it) } ?: this.status,
    )
```

---

## Dependencies

**Imports:**
- Domain models from `atlas-project-backend-common`
- DTOs from `atlas-project-backend-transport`
- `kotlinx.datetime.LocalDate` for date handling

**Imported by:**
- Ktor App module (for request/response conversion)
- Routing handlers

---

## Usage Example

```kotlin
// In Routing.kt
post("/project-tasks") {
    val request = call.receive<CreateProjectTaskRequest>()
    
    // Convert DTO to domain
    val domainTask = request.toModel()
    
    // Business logic
    val created = config.repo.createTask(domainTask)
    
    // Convert domain to DTO
    val response = created.toScheduledTaskDto(startDate, endDate)
    
    call.respond(HttpStatusCode.Created, response)
}
```

---

## Design Principles

### Separation of Concerns

- **Domain models** know nothing about API contracts
- **DTOs** know nothing about business logic
- **Mappers** bridge the gap

### Type Safety

- Value objects (`TaskId`, `Title`, `Duration`) prevent invalid data
- Extension functions provide discoverable conversions

### Immutability

- Domain models are data classes (immutable by default)
- Conversions create new instances, not mutations

---

## Related Files

- [Common Module](./common.md)
- [Transport Module](./transport.md)
- [Ktor App Module](./ktor-app.md)
