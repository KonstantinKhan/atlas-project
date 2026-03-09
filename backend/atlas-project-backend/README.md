# Atlas Project Backend

**Path:** `/backend/atlas-project-backend`  
**Last Updated:** 2026-03-09

## Overview

Multi-module Kotlin backend for the Atlas Project Gantt Chart application. Provides REST API for project planning, task management, dependency tracking, and critical path method (CPM) analysis. Built with Ktor, Exposed ORM, and PostgreSQL.

## Architecture

The backend follows a **clean architecture** pattern with clear separation of concerns across multiple modules:

```
┌─────────────────────────────────────────────────────────┐
│                    Ktor Application                      │
│              (atlas-project-backend-ktor-app)            │
├─────────────────────────────────────────────────────────┤
│                     Transport Layer                      │
│            (atlas-project-backend-transport)             │
├─────────────────────────────────────────────────────────┤
│                      Mappers Layer                       │
│              (atlas-project-backend-mappers)             │
├─────────────────────────────────────────────────────────┤
│                   Calendar Service                       │
│         (atlas-project-backend-calendar-service)         │
├─────────────────────────────────────────────────────────┤
│                   Repository Layer                       │
│    (postgres)              │       (in-memory)           │
│  (atlas-project-backend-  │  (atlas-project-backend-    │
│         postgres)         │        repo-in-memory)       │
├─────────────────────────────────────────────────────────┤
│                      Common Layer                        │
│            (atlas-project-backend-common)                │
│   Domain Models │ Enums │ Repository Interfaces          │
└─────────────────────────────────────────────────────────┘
```

## Modules

| Module | Purpose | Key Technologies |
|--------|---------|-----------------|
| **ktor-app** | REST API server, routing, HTTP configuration | Ktor, Netty, Flyway |
| **transport** | DTOs for API requests/responses, TypeScript generation | Kotlinx Serialization |
| **mappers** | Domain ↔ Transport object mapping | Kotlin |
| **calendar-service** | Working calendar calculations, holiday handling | Kotlinx DateTime |
| **postgres** | PostgreSQL repository implementation | Exposed, PostgreSQL JDBC |
| **repo-in-memory** | In-memory repository for testing | Exposed, H2 |
| **common** | Domain models, enums, repository interfaces | Kotlin Multiplatform |

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin | 2.x (JVM + Multiplatform) |
| **Build Tool** | Gradle | 8.x with Kotlin DSL |
| **Web Framework** | Ktor | 3.x |
| **Server** | Netty | Embedded |
| **Database** | PostgreSQL | 42.x JDBC driver |
| **ORM** | JetBrains Exposed | 0.x |
| **Migrations** | Flyway | 10.x |
| **Serialization** | Kotlinx Serialization | 1.x |
| **DateTime** | Kotlinx DateTime | 0.x |
| **Testing** | JUnit, Ktor Test | - |
| **Logging** | Logback | - |
| **In-Memory DB** | H2 | Testing |

## Project Structure

```
atlas-project-backend/
├── atlas-project-backend-ktor-app/       # Main application
│   ├── src/main/kotlin/
│   │   ├── com/khan366kos/atlas/project/backend/ktor/app/
│   │   │   ├── Application.kt            # Entry point
│   │   │   ├── plugins/
│   │   │   │   └── Routing.kt            # API routes configuration
│   │   │   └── routes/
│   │   │       ├── CriticalPath.kt       # CPM endpoint
│   │   │       └── ProjectPlan.kt        # Project plan endpoint
│   │   ├── config/
│   │   │   └── AppConfig.kt              # Configuration
│   │   ├── Databases.kt                  # Database setup
│   │   ├── HTTP.kt                       # HTTP configuration
│   │   ├── Mappers.kt                    # Global mappers
│   │   ├── Routing.kt                    # Route definitions
│   │   ├── Serialization.kt              # JSON configuration
│   │   ├── StatusPages.kt                # Error handling
│   │   └── WorkCalendarUtils.kt          # Calendar utilities
│   └── build.gradle.kts
├── atlas-project-backend-transport/       # DTOs
│   ├── src/commonMain/kotlin/
│   │   └── com/khan366kos/atlas/project/backend/transport/
│   │       ├── GanttTaskDto.kt
│   │       ├── GanttDependencyDto.kt
│   │       ├── TaskDto.kt
│   │       ├── ScheduledTaskDto.kt
│   │       ├── commands/                  # Command DTOs
│   │       ├── enums/                     # Enum DTOs
│   │       ├── plan/                      # Plan DTOs
│   │       ├── timelineCalendar/          # Calendar DTOs
│   │       └── GenerateTypeScript.kt      # TS generator
│   └── build.gradle.kts
├── atlas-project-backend-mappers/         # Object mapping
│   ├── src/main/kotlin/
│   │   └── com/khan366kos/atlas/project/backend/mappers/
│   │       ├── DomainToTransport.kt
│   │       └── TransportToDomain.kt
│   └── build.gradle.kts
├── atlas-project-backend-calendar-service/ # Calendar logic
│   ├── src/main/kotlin/
│   │   └── com/khan366kos/atlas/project/backend/calendar/service/
│   │       └── CacheCalendarProvider.kt
│   └── build.gradle.kts
├── atlas-project-backend-postgres/        # PostgreSQL repo
│   ├── src/main/kotlin/
│   │   └── com/khan366kos/atlas/project/backend/repo/postgres/
│   │       ├── AtlasProjectTaskRepoPostgres.kt
│   │       ├── ProjectTasksTable.kt
│   │       ├── table/                     # Table definitions
│   │       └── mapper/                    # Row mappers
│   └── build.gradle.kts
├── atlas-project-backend-repo-in-memory/  # In-memory repo
│   ├── src/main/kotlin/
│   │   └── com/khan366kos/atlas/project/backend/repo/inmemory/
│   │       ├── AtlasProjectTaskRepoInMemory.kt
│   │       └── ProjectTasksTable.kt
│   └── build.gradle.kts
├── atlas-project-backend-common/          # Domain models
│   ├── src/commonMain/kotlin/
│   │   └── com/khan366kos/atlas/project/backend/common/
│   │       ├── models/
│   │       │   ├── task/
│   │       │   ├── taskSchedule/
│   │       │   ├── projectPlan/
│   │       │   └── timelineCalendar/
│   │       ├── enums/
│   │       ├── repo/                      # Repository interfaces
│   │       └── simple/                    # Value objects
│   └── build.gradle.kts
├── build.gradle.kts                       # Root build config
├── settings.gradle.kts                    # Module settings
├── gradle.properties                      # Gradle properties
└── README.md                              # This file
```

## Getting Started

### Prerequisites

- JDK 17 or later
- PostgreSQL 14+ (for production)
- Gradle 8.x

### Configuration

Create `atlas-project-backend-ktor-app/src/main/resources/application.conf`:

```hocon
ktor {
    application {
        modules = [com.khan366kos.atlas.project.backend.ktor.app.ApplicationKt.module]
    }
    deployment {
        port = 8080
    }
    database {
        url = "jdbc:postgresql://localhost:5432/atlas_project"
        user = "postgres"
        password = "postgres"
    }
}
```

### Running the Application

```bash
# Build all modules
./gradlew build

# Run the application
./gradlew :atlas-project-backend-ktor-app:run

# Run tests
./gradlew test

# Generate TypeScript types
./gradlew :atlas-project-backend-transport:generateTypeScript
```

## API Endpoints

### Project Tasks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/project-tasks` | Get all tasks |
| POST | `/project-tasks/create-in-pool` | Create new task |
| PATCH | `/project-tasks/:id` | Update task |
| DELETE | `/project-tasks/:id` | Delete task |
| POST | `/project-tasks/:id/schedule` | Assign schedule |

### Project Plan

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/project-plan` | Get full project plan |
| POST | `/change-start` | Change task start date |
| POST | `/change-end` | Change task end date |
| POST | `/resize-from-start` | Resize from start |
| POST | `/plan-from-end` | Plan backward from end |

### Dependencies

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/dependencies` | Create dependency |
| DELETE | `/dependencies?from=:from&to=:to` | Remove dependency |
| PATCH | `/dependencies` | Change dependency type |

### Critical Path

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/critical-path` | Get CPM analysis |

### Timeline Calendar

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/timeline-calendar` | Get calendar config |
| PUT | `/timeline-calendar` | Update calendar |

## Domain Models

### Core Models (common module)

```kotlin
// Task model
data class ProjectTask(
    val id: TaskId,
    val title: Title,
    val description: Description,
    val duration: Duration,
    val status: ProjectTaskStatus
)

// Task Schedule
data class TaskSchedule(
    val id: TaskScheduleId,
    val taskId: ProjectTaskId,
    val start: ProjectDate,
    val end: ProjectDate
)

// Task Dependency
data class TaskDependency(
    val fromTaskId: ProjectTaskId,
    val toTaskId: ProjectTaskId,
    val type: DependencyType  // FS, SS, FF, SF
)

// Timeline Calendar
data class TimelineCalendar(
    val id: String,
    val name: String,
    val workingWeek: List<DayOfWeek>,
    val holidays: List<Holiday>,
    val workingWeekends: List<LocalDate>
)
```

### Enums

```kotlin
enum class DependencyType {
    FS,  // Finish-Start
    SS,  // Start-Start
    FF,  // Finish-Finish
    SF   // Start-Finish
}

enum class ProjectTaskStatus {
    EMPTY,
    PLANNED,
    IN_PROGRESS,
    COMPLETED
}
```

## Repository Pattern

The `common` module defines repository interfaces:

```kotlin
interface IAtlasProjectTaskRepo {
    suspend fun getAll(): List<ProjectTask>
    suspend fun getById(id: TaskId): ProjectTask?
    suspend fun create(task: ProjectTask): ProjectTask
    suspend fun update(task: ProjectTask): ProjectTask
    suspend fun delete(id: TaskId)
    // ... schedule and dependency operations
}
```

Implementations:
- `AtlasProjectTaskRepoPostgres` - Production PostgreSQL implementation
- `AtlasProjectTaskRepoInMemory` - Testing implementation with H2

## Critical Path Method (CPM)

The `common` module includes CPM implementation:

```kotlin
// CriticalPathAnalysis.kt
object CriticalPathAnalysis {
    fun calculate(tasks: List<Task>, dependencies: List<Dependency>): CriticalPath {
        // Topological sort
        // Forward pass (early start/finish)
        // Backward pass (late start/finish)
        // Calculate slack
        // Identify critical path (zero slack)
    }
}
```

## TypeScript Generation

DTOs are annotated with `@Serializable` for automatic TypeScript generation:

```kotlin
@Serializable
data class GanttTaskDto(
    val id: String,
    val title: String,
    val description: String,
    val start: LocalDate?,
    val end: LocalDate?,
    val status: ProjectTaskStatus
)
```

Generated TypeScript types are placed in:
`frontend/atlas-project-web-app/src/types/generated/`

## Database Schema

Tables managed by Exposed ORM:

- `project_plans` - Project plan metadata
- `project_tasks` - Task definitions
- `task_schedules` - Task scheduling
- `task_dependencies` - Task dependencies
- `timeline_calendars` - Calendar definitions
- `timeline_calendar_holidays` - Calendar holidays
- `timeline_calendar_working_weekends` - Working weekends

Migrations managed by Flyway in `src/main/resources/db/migration/`.

## Testing

```kotlin
// Example test
class RoutingTest {
    @Test
    fun testGetProjectTasks() = testApplication {
        val response = client.get("/project-tasks")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
```

Run tests:
```bash
./gradlew test
```

## Dependency Graph

```
ktor-app
├── transport
├── common
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

## Related Documentation

- [Frontend Documentation](../../frontend/atlas-project-web-app/README.md)
- [Project Overview](../../docs/project/)
