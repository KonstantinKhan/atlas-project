# Backend - Index

**Path:** `/backend/atlas-project-backend`  
**Parent:** [Project Documentation Map](../PROJECT_MAP.md)  
**Last Updated:** 2026-03-03

## Module Structure

```
atlas-project-backend/
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Module includes
├── gradle.properties             # Gradle properties
└── [modules]/
    ├── atlas-project-backend-ktor-app/       # HTTP server
    ├── atlas-project-backend-common/         # Domain models
    ├── atlas-project-backend-transport/      # DTOs
    ├── atlas-project-backend-mappers/        # Mappings
    ├── atlas-project-backend-calendar-service/ # Calendar
    ├── atlas-project-backend-postgres/       # PostgreSQL repo
    └── atlas-project-backend-repo-in-memory/ # In-memory repo
```

## Modules Overview

| Module | Path | Description | Links |
|--------|------|-------------|-------|
| **Ktor App** | `./atlas-project-backend-ktor-app/` | HTTP server with REST API endpoints | [Detail](details/ktor-app.md) |
| **Common** | `./atlas-project-backend-common/` | Domain models and repository interfaces | [Detail](details/common.md) |
| **Transport** | `./atlas-project-backend-transport/` | Data transfer objects (DTOs) | [Detail](details/transport.md) |
| **Mappers** | `./atlas-project-backend-mappers/` | Domain ↔ Transport conversion | [Detail](details/mappers.md) |
| **Calendar Service** | `./atlas-project-backend-calendar-service/` | Work calendar logic | [Detail](details/calendar-service.md) |
| **Postgres** | `./atlas-project-backend-postgres/` | PostgreSQL repository implementation | [Detail](details/postgres.md) |
| **Repo In-Memory** | `./atlas-project-backend-repo-in-memory/` | In-memory repository for testing | [Detail](details/repo-in-memory.md) |

## Module Dependencies

```
atlas-project-backend-ktor-app
    ├── atlas-project-backend-common
    ├── atlas-project-backend-transport
    ├── atlas-project-backend-mappers
    ├── atlas-project-backend-calendar-service
    └── atlas-project-backend-postgres (or in-memory)

atlas-project-backend-mappers
    ├── atlas-project-backend-common
    └── atlas-project-backend-transport

atlas-project-backend-postgres
    └── atlas-project-backend-common

atlas-project-backend-repo-in-memory
    └── atlas-project-backend-common

atlas-project-backend-calendar-service
    └── (standalone)

atlas-project-backend-transport
    └── (standalone)

atlas-project-backend-common
    └── (standalone)
```

## API Endpoints

### Calendar

| Endpoint | Method | Module | Handler |
|----------|--------|--------|---------|
| `/work-calendar` | GET | ktor-app | `configureRouting` |

### Project Plan

| Endpoint | Method | Module | Handler |
|----------|--------|--------|---------|
| `/project-plan` | GET | ktor-app | `configureRouting` |
| `/change-start` | POST | ktor-app | `configureRouting` |
| `/change-end` | POST | ktor-app | `configureRouting` |

### Dependencies

| Endpoint | Method | Module | Handler |
|----------|--------|--------|---------|
| `/dependencies` | POST | ktor-app | `configureRouting` |
| `/dependencies/recalculate` | POST | ktor-app | `configureRouting` |

### Tasks

| Endpoint | Method | Module | Handler |
|----------|--------|--------|---------|
| `/project-tasks` | POST | ktor-app | `configureRouting` |
| `/project-tasks/create-in-pool` | POST | ktor-app | `configureRouting` |
| `/project-tasks/:id` | PATCH | ktor-app | `configureRouting` |
| `/project-tasks/:id` | DELETE | ktor-app | `configureRouting` |

## Domain Models

### Core Models (common)

| Model | Path | Description |
|-------|------|-------------|
| `ProjectTask` | `common/models/task/` | Task entity with id, title, description, duration, status |
| `TaskSchedule` | `common/models/taskSchedule/` | Scheduled dates for a task |
| `TaskDependency` | `common/models/` | Dependency between tasks |
| `ProjectPlan` | `common/models/projectPlan/` | Complete project plan |
| `ProjectDate` | `common/models/` | Date wrapper (Set/Unset) |
| `Duration` | `common/models/simple/` | Duration value object |

### Repository Interfaces

| Interface | Path | Description |
|-----------|------|-------------|
| `IAtlasProjectTaskRepo` | `common/repo/` | Repository interface for task operations |

## Transport DTOs

### Task DTOs (transport)

| DTO | Description |
|-----|-------------|
| `TaskDto` | Basic task data |
| `GanttTaskDto` | Task with schedule for Gantt |
| `ScheduledTaskDto` | Task with scheduled dates |
| `CreateProjectTaskRequest` | Task creation request |
| `UpdateProjectTaskRequest` | Task update request |

### Command DTOs (transport/commands)

| DTO | Description |
|-----|-------------|
| `ChangeTaskStartDateCommandDto` | Start date change command |
| `ChangeTaskEndDateCommandDto` | End date change command |
| `CreateDependencyCommandDto` | Dependency creation command |
| `CreateTaskInPoolCommandDto` | Pool task creation command |

### Calendar DTOs (transport/calendar)

| DTO | Description |
|-----|-------------|
| `WorkCalendar` | Work calendar configuration |
| `TimelineCalendarDto` | Timeline calendar for UI |

## Dependencies

### External Dependencies

| Dependency | Purpose |
|------------|---------|
| `io.ktor:ktor-server-*` | HTTP server |
| `io.ktor:ktor-serialization-*` | JSON serialization |
| `org.jetbrains.kotlinx:kotlinx-datetime` | Date/time handling |
| `org.jetbrains.kotlinx:kotlinx-serialization` | Serialization |
| `org.postgresql:postgresql` | PostgreSQL driver |

### Internal Dependencies

See [Module Dependencies](#module-dependencies) above.

---

## Navigation

- [Back to Overview](OVERVIEW.md)
- [Details Directory](details/)
- [Project Map](../PROJECT_MAP.md)
