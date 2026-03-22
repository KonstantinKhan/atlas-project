# Ktor App Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-ktor-app/`
**Module:** [Backend Index](../INDEX.md)
**Last Updated:** 2026-03-06

## Purpose

The Ktor App module is the HTTP server entry point. It configures the Ktor server with routing, serialization, error handling, and dependency injection.

## Files Overview

| File | Purpose |
|------|---------|
| `Application.kt` | Application entry point and module configuration |
| `Routing.kt` | REST API endpoint definitions |
| `Serialization.kt` | JSON serialization configuration |
| `HTTP.kt` | HTTP configuration (CORS, headers) |
| `StatusPages.kt` | Error handling and status pages |
| `Databases.kt` | Database connection configuration |
| `Mappers.kt` | Additional mapper configurations |
| `WorkCalendarUtils.kt` | Calendar utility functions |
| `config/AppConfig.kt` | Application configuration |

---

## Application.kt

### Purpose

Main entry point for the Ktor application. Configures all server components.

### Key Functions

```kotlin
fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(
    config: AppConfig = AppConfig(environment)
) {
    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureRouting(config)
}
```

### Dependencies

**Imports:**
- `com.khan366kos.config.AppConfig`
- `io.ktor.server.application.*`
- `io.ktor.server.netty.EngineMain`

**Imported by:**
- Entry point for the application

---

## Routing.kt

### Purpose

Defines all REST API endpoints for the application.

### Endpoints

#### GET /work-calendar

**Purpose:** Get the current work calendar.

**Response:** `TimelineCalendar` DTO

**Handler:**
```kotlin
get("/work-calendar") {
    val timelineCalendar = config.calendarService.current()
    call.respond(timelineCalendar.toTransport())
}
```

---

#### GET /project-plan

**Purpose:** Get the complete project plan with tasks and dependencies.

**Response:** `GanttProjectPlan` DTO

**Handler:**
```kotlin
get("/project-plan") {
    val plan = config.repo.projectPlan()
    call.respond(plan.toGanttDto())
}
```

---

#### POST /change-start

**Purpose:** Change a task's start date and recalculate schedule.

**Request:** `ChangeTaskStartDateCommandDto`

**Response:** `ScheduleDelta` DTO

**Handler:**
```kotlin
post("/change-start") {
    val request = call.receive<ChangeTaskStartDateCommandDto>()
    val plan = config.repo.projectPlan()
    val delta = plan.changeTaskStartDate(
        taskId = TaskId(request.taskId),
        newStart = request.newPlannedStart,
        calendar = config.calendarService.current()
    )
    // Persist changes
    call.respond(delta.toDto())
}
```

---

#### POST /change-end

**Purpose:** Change a task's end date and recalculate schedule.

**Request:** `ChangeTaskEndDateCommandDto`

**Response:** `ScheduleDelta` DTO

---

#### POST /resize-from-start

**Purpose:** Resize a task from its start date (left edge drag). Changes the start date while keeping the end date fixed, effectively changing the duration.

**Request:** `ChangeTaskStartDateCommandDto`

```kotlin
data class ChangeTaskStartDateCommandDto(
    val taskId: String,
    val newPlannedStart: String,  // ISO date
)
```

**Response:** `ScheduleDelta` DTO

**Handler:**
```kotlin
post("/resize-from-start") {
    val request = call.receive<ChangeTaskStartDateCommandDto>()
    val plan = config.repo.projectPlan()
    val delta = plan.resizeTaskFromStart(
        taskId = TaskId(request.taskId),
        newStart = request.newPlannedStart,
        calendar = config.calendarService.current()
    )
    delta.updatedSchedule.forEach {
        plan.schedules()[it.id] = it
        config.repo.updateSchedule(it)
    }
    // Update task duration
    plan.tasks().find { it.id == TaskId(request.taskId) }
        ?.let { config.repo.updateTask(it) }
    call.respond(delta.toDto())
}
```

**Implementation Details:**
- Uses `ProjectPlan.resizeTaskFromStart()` which calculates new duration while preserving end date
- Respects dependency constraints (FS/SS clamping)
- Updates task duration in repository
- Returns schedule delta with all affected schedules

---

#### POST /dependencies

**Purpose:** Create a dependency between two tasks.

**Request:** `CreateDependencyCommandDto`

**Response:** `GanttProjectPlan` DTO (updated plan)

**Handler:**
```kotlin
post("/dependencies") {
    val request = call.receive<CreateDependencyCommandDto>()
    val plan = config.repo.projectPlan()
    val delta = plan.addDependency(
        predecessorId = TaskId(request.fromTaskId),
        successorId = TaskId(request.toTaskId),
        type = request.type.toDomain(),
        lagDays = request.lagDays,
        calendar = config.calendarService.current()
    )
    // Persist dependency and schedules
    call.respond(plan.toGanttDto())
}
```

---

#### POST /dependencies/recalculate

**Purpose:** Recalculate all task schedules based on dependencies.

**Algorithm:** Uses `ProjectPlan.recalculateAll()` which iterates through all tasks and applies constraint-based scheduling via `calculateConstrainedStart()`.

**Response:** `GanttProjectPlan` DTO (updated plan)

**Handler:**
```kotlin
post("/dependencies/recalculate") {
    val plan = config.repo.projectPlan()
    val calendar = config.calendarService.current()
    val delta = plan.recalculateAll(calendar)
    delta.updatedSchedule.forEach { config.repo.updateSchedule(it) }
    call.respond(config.repo.projectPlan().toGanttDto())
}
```

**Implementation Details:**
- Replaces previous BFS-based cascade recalculation
- Uses unified `calculateConstrainedStart()` formula for all tasks
- Handles all dependency types (FS, SS, FF, SF) with lag support

---

#### PATCH /dependencies

**Purpose:** Change the type of an existing dependency (e.g., from FS to SS).

**Request:** `ChangeDependencyTypeCommandDto`

```kotlin
data class ChangeDependencyTypeCommandDto(
    val fromTaskId: String,
    val toTaskId: String,
    val newType: String,  // FS, SS, FF, SF
)
```

**Response:** `GanttProjectPlan` DTO (updated plan)

**Handler:**
```kotlin
patch("/dependencies") {
    val request = call.receive<ChangeDependencyTypeCommandDto>()
    val plan = config.repo.projectPlan()
    val calendar = config.calendarService.current()

    val delta = plan.changeDependencyType(
        predecessorId = TaskId(request.fromTaskId),
        successorId = TaskId(request.toTaskId),
        newType = request.newType.toDomain(),
        calendar = calendar
    )

    // Persist updated dependency type and lag
    val updatedDep = plan.dependencies()
        .find { it.predecessor == TaskId(request.fromTaskId) && it.successor == TaskId(request.toTaskId) }
    if (updatedDep != null) {
        config.repo.updateDependency(
            predecessorId = request.fromTaskId,
            successorId = request.toTaskId,
            type = updatedDep.type.name,
            lagDays = updatedDep.lag.asInt()
        )
    }
    delta.updatedSchedule.forEach { config.repo.updateSchedule(it) }

    call.respond(plan.toGanttDto())
}
```

**Implementation Details:**
- Uses `ProjectPlan.changeDependencyType()` to recalculate schedules with new dependency type
- Persists updated dependency type and lag to repository
- Returns updated project plan with recalculated schedules
- Frontend: Triggered by `DependencyActionPopover` component

---

#### DELETE /dependencies

**Purpose:** Remove a dependency between two tasks.

**Query Parameters:**
- `from` - Predecessor task ID
- `to` - Successor task ID

**Response:** `GanttProjectPlan` DTO (updated plan)

**Error Responses:**
- `400 Bad Request` - Missing 'from' or 'to' parameter

**Handler:**
```kotlin
delete("/dependencies") {
    val from = call.request.queryParameters["from"]
        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing 'from' parameter")
    val to = call.request.queryParameters["to"]
        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing 'to' parameter")

    val plan = config.repo.projectPlan()
    val calendar = config.calendarService.current()

    val delta = plan.removeDependency(
        predecessorId = TaskId(from),
        successorId = TaskId(to),
        calendar = calendar
    )

    config.repo.deleteDependency(from, to)
    delta.updatedSchedule.forEach { config.repo.updateSchedule(it) }

    call.respond(plan.toGanttDto())
}
```

**Implementation Details:**
- Uses `ProjectPlan.removeDependency()` to recalculate schedules after dependency removal
- Successor tasks may move earlier (backward) when constraint is removed
- Persists dependency deletion to repository
- Returns updated project plan
- Frontend: Triggered by delete button in `DependencyActionPopover`

---

#### POST /project-tasks

**Purpose:** Create a new project task with initial schedule.

**Request:** `CreateProjectTaskRequest`

**Response:** `ScheduledTaskDto` (201 Created)

**Handler:**
```kotlin
post("/project-tasks") {
    val request = call.receive<CreateProjectTaskRequest>()
    val created = config.repo.createTask(request.toModel())
    
    // Calculate initial schedule
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate = calendar.currentOrNextWorkingDay(today)
    val endDate = calendar.addWorkingDays(startDate, duration)
    
    config.repo.updateSchedule(TaskSchedule(...))
    call.respond(HttpStatusCode.Created, created.toScheduledTaskDto(startDate, endDate))
}
```

---

#### POST /project-tasks/create-in-pool

**Purpose:** Create a task in the pool (without schedule).

**Request:** `CreateTaskInPoolCommandDto`

**Response:** `TaskDto` (201 Created)

---

#### PATCH /project-tasks/:id

**Purpose:** Update an existing task.

**Request:** `UpdateProjectTaskRequest`

**Response:** `TaskDto`

**Handler:**
```kotlin
patch("/project-tasks/{id}") {
    val id = call.parameters["id"]
    val request = call.receive<UpdateProjectTaskRequest>()
    val existing = config.repo.getTask(id)
    val updated = existing.applyUpdate(request)
    call.respond(config.repo.updateTask(updated).toTaskDto())
}
```

---

#### DELETE /project-tasks/:id

**Purpose:** Delete an existing task and all associated schedules and dependencies.

**Response:** `204 No Content` on success

**Error Responses:**
- `400 Bad Request` - Task ID is missing or empty
- `404 Not Found` - Task with specified ID does not exist

**Handler:**
```kotlin
delete("/project-tasks/{id}") {
    val id = call.parameters["id"]
        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

    if (id.isBlank()) {
        return@delete call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
    }

    config.repo.getTask(id)
        ?: return@delete call.respond(HttpStatusCode.NotFound)

    config.repo.deleteTask(id)
    call.respond(HttpStatusCode.NoContent)
}
```

**Implementation Details:**
- Validates that the task ID is provided and not empty
- Checks if the task exists before attempting deletion (returns 404 if not found)
- Calls `repo.deleteTask(id)` which cascade deletes schedules and dependencies
- Returns 204 No Content on successful deletion (no response body)

---

#### POST /project-tasks/{id}/schedule

**Purpose:** Assign a schedule to an existing task (typically for pool tasks).

**Request:** `AssignScheduleCommandDto`

```kotlin
data class AssignScheduleCommandDto(
    val start: String,      // ISO date string
    val duration: Int,
)
```

**Response:** `GanttProjectPlan` DTO (updated plan)

**Error Responses:**
- `400 Bad Request` - Task ID is missing or empty
- `404 Not Found` - Task with specified ID does not exist

**Handler:**
```kotlin
post("/project-tasks/{id}/schedule") {
    val id = call.parameters["id"]
        ?: return@post call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

    if (id.isBlank()) {
        return@post call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
    }

    config.repo.getTask(id)
        ?: return@post call.respond(HttpStatusCode.NotFound)

    val request = call.receive<AssignScheduleCommandDto>()
    val calendar = config.calendarService.current()
    val startDate = calendar.currentOrNextWorkingDay(LocalDate.parse(request.start))
    val endDate = calendar.addWorkingDays(startDate, Duration(request.duration))

    config.repo.updateSchedule(
        TaskSchedule(
            id = TaskScheduleId(id),
            start = ProjectDate.Set(startDate),
            end = ProjectDate.Set(endDate),
        )
    )
    call.respond(config.repo.projectPlan().toGanttDto())
}
```

**Implementation Details:**
- Validates task ID parameter
- Verifies task exists before assigning schedule
- Parses start date from ISO string
- Calculates end date using calendar service: `calendar.addWorkingDays(startDate, duration)`
- Updates schedule in repository
- Returns updated project plan (`GanttProjectPlan` DTO)

---

#### POST /plan-from-end

**Purpose:** Plan a task backwards from its end date (useful for deadline-driven scheduling).

**Request:** `PlanFromEndCommandDto`

```kotlin
data class PlanFromEndCommandDto(
    val taskId: String,
    val newPlannedEnd: String,  // ISO date string
)
```

**Response:** `GanttProjectPlanDto` (updated plan)

**Handler:**
```kotlin
post("/plan-from-end") {
    val request = call.receive<PlanFromEndCommandDto>()
    val plan = config.repo.projectPlan()
    val calendar = config.calendarService.current()
    val delta = plan.planFromEnd(
        taskId = TaskId(request.taskId),
        newEnd = LocalDate.parse(request.newPlannedEnd),
        calendar = calendar
    )
    delta.updatedSchedule.forEach {
        config.repo.updateSchedule(it)
    }
    call.respond(plan.toGanttDto())
}
```

**Implementation Details:**
- Calculates start date by working backwards from end date using `TimelineCalendar.subtractWorkingDays()`
- Respects working days and holidays
- Updates all affected schedules via `plan.planFromEnd()`
- Persists changes to repository
- Returns updated project plan

---

### Dependencies

**Imports:**
- Domain models from `common`
- DTOs from `transport`
- Mappers from `mappers`
- Repository interface from `common/repo`
- Ktor routing functions

**Imported by:**
- `Application.kt`

---

## Serialization.kt

### Purpose

Configures JSON serialization for request/response bodies.

### Configuration

```kotlin
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
```

---

## HTTP.kt

### Purpose

Configures HTTP server settings including CORS.

### Configuration

```kotlin
fun Application.configureHTTP() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.ContentType)
    }
}
```

---

## Resource Routes

### Resources Route (`routes/Resources.kt`)

#### GET /resources

**Purpose:** List all resources for the project plan.

**Response:** `ResourceListDto`

**Handler:**
```kotlin
get {
    val plan = taskRepo.projectPlan()
    val resources = resourceRepo.listResources(plan.id.asString())
    call.respond(ResourceListDto(resources = resources.map { it.toDto() }))
}
```

---

#### POST /resources

**Purpose:** Create a new resource.

**Request:** `CreateResourceCommandDto`

**Response:** `ResourceDto` (201 Created)

**Handler:**
```kotlin
post {
    val request = call.receive<CreateResourceCommandDto>()
    val plan = taskRepo.projectPlan()
    val resource = Resource(
        name = ResourceName(request.name),
        type = ResourceType.valueOf(request.type),
        capacityHoursPerDay = request.capacityHoursPerDay,
    )
    val created = resourceRepo.createResource(plan.id.asString(), resource)
    call.respond(HttpStatusCode.Created, created.toDto())
}
```

---

#### PATCH /resources/:id

**Purpose:** Update an existing resource.

**Request:** `UpdateResourceCommandDto`

**Response:** `ResourceDto`

---

#### DELETE /resources/:id

**Purpose:** Delete a resource.

**Response:** `204 No Content`

---

#### GET/POST/DELETE /resources/:id/calendar-overrides

**Purpose:** Manage calendar overrides for a resource.

**GET Response:** `ResourceCalendarOverrideListDto`

**POST Request:** `ResourceCalendarOverrideDto`

**POST Response:** `ResourceCalendarOverrideDto` (201 Created)

---

### Assignments Route (`routes/Assignments.kt`)

#### GET /assignments

**Purpose:** List all task assignments.

**Response:** `TaskAssignmentListDto`

---

#### POST /assignments

**Purpose:** Create a new task assignment.

**Request:** `CreateAssignmentCommandDto`

**Response:** `TaskAssignmentDto` (201 Created)

---

#### PATCH /assignments/:id

**Purpose:** Update an existing assignment.

**Request:** `UpdateAssignmentCommandDto`

**Response:** `TaskAssignmentDto`

---

#### DELETE /assignments/:id

**Purpose:** Delete an assignment.

**Response:** `204 No Content`

---

#### GET/POST/DELETE /assignments/:id/day-overrides

**Purpose:** Manage day overrides for an assignment.

**GET Response:** `AssignmentDayOverrideListDto`

**POST Request:** `SetDayOverrideCommandDto`

---

### Resource Load Route

#### GET /resource-load

**Purpose:** Get resource overload report for a date range.

**Query Parameters:**
- `from` - Start date (ISO string)
- `to` - End date (ISO string)

**Response:** `OverloadReportDto`

**Handler:**
```kotlin
get {
    val from = LocalDate.parse(call.request.queryParameters["from"]!!)
    val to = LocalDate.parse(call.request.queryParameters["to"]!!)
    val plan = taskRepo.projectPlan()
    val assignments = resourceRepo.listAssignments(plan.id.asString())
    val resources = resourceRepo.listResources(plan.id.asString())
    val calendar = calendarService.current()
    val calculator = ResourceLoadCalculator(...)
    val report = calculator.computeLoad(from, to)
    call.respond(report.toDto())
}
```

---

#### GET /resource-load/:resourceId

**Purpose:** Get load details for a specific resource.

**Response:** `ResourceLoadResultDto`

---

### Leveling Route (`routes/Leveling.kt`)

#### POST /leveling/preview

**Purpose:** Preview resource leveling result without applying changes.

**Response:** `LevelingResultDto`

**Handler:**
```kotlin
post("/preview") {
    val plan = taskRepo.projectPlan()
    val engine = ResourceLevelingEngine(...)
    val result = engine.level()
    call.respond(HttpStatusCode.OK, result.toDto())
}
```

---

#### POST /leveling/apply

**Purpose:** Apply resource leveling changes to the project schedule.

**Response:** `LevelingResultDto`

**Handler:**
```kotlin
post("/apply") {
    val plan = taskRepo.projectPlan()
    val engine = ResourceLevelingEngine(...)
    val result = engine.level()
    
    // Persist schedule changes
    for (schedule in result.scheduleDelta.updatedSchedule) {
        taskRepo.updateSchedule(schedule)
    }
    
    call.respond(HttpStatusCode.OK, result.toDto())
}
```

---

### Analysis Route (`routes/Analysis.kt`)

#### GET /analysis/blocker-chain/:taskId

**Purpose:** Get the chain of blocking tasks preventing a task from starting earlier.

**Response:** `BlockerChainDto`

**Handler:**
```kotlin
get("/blocker-chain/{taskId}") {
    val taskId = TaskId(call.parameters["taskId"]!!)
    val plan = repo.projectPlan()
    call.respond(plan.blockerChain(taskId).toDto())
}
```

---

#### GET /analysis/available-tasks

**Purpose:** Get tasks available to start from a given date.

**Query Parameters:**
- `today` - Reference date

**Response:** `AvailableTasksDto`

---

#### GET /analysis/what-if

**Purpose:** Simulate impact of changing a task's start date.

**Query Parameters:**
- `taskId` - Task to analyze
- `newStart` - New start date

**Response:** `WhatIfDto`

---

#### GET /analysis/what-if-end

**Purpose:** Simulate impact of changing a task's end date.

**Response:** `WhatIfDto`

---

### Critical Path Route (`routes/CriticalPath.kt`)

#### GET /critical-path

**Purpose:** Calculate the critical path through the project.

**Response:** `CriticalPathDto`

**Handler:**
```kotlin
get {
    val plan = repo.projectPlan()
    val calendar = calendarService.current()
    val result = CriticalPathAnalysis(plan, calendar).compute()
    call.respond(result.toDto())
}
```

---

## StatusPages.kt

### Purpose

Configures error handling and exception mapping.

### Configuration

```kotlin
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, 
                mapOf("error" to cause.message))
        }
    }
}
```

---

## AppConfig.kt

### Purpose

Application configuration loaded from environment.

### Properties

```kotlin
class AppConfig(environment: ApplicationEnvironment) {
    val calendarService: CalendarService
    val repo: IAtlasProjectTaskRepo
    // ... other configuration
}
```

---

## Related Files

- [Common Module](./common.md)
- [Transport Module](./transport.md)
- [Mappers Module](./mappers.md)
- [Calendar Service](./calendar-service.md)
- [Postgres Module](./postgres.md)
