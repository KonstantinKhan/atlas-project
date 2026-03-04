# Ktor App Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-ktor-app/`
**Module:** [Backend Index](../INDEX.md)
**Last Updated:** 2026-03-04

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

**Algorithm:** BFS through dependency graph starting from root tasks.

**Response:** `GanttProjectPlan` DTO (updated plan)

**Handler:**
```kotlin
post("/dependencies/recalculate") {
    val plan = config.repo.projectPlan()
    val calendar = config.calendarService.current()
    
    // Find root tasks (no predecessors)
    // BFS through dependency graph
    // Update schedules for affected tasks
    // Persist changes
    
    call.respond(updatedPlan.toGanttDto())
}
```

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
