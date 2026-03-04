# Backend - Overview

**Path:** `/backend/atlas-project-backend`  
**Parent:** [Project Documentation Map](../PROJECT_MAP.md)  
**Last Updated:** 2026-03-03

## Purpose

The backend is a **Kotlin multiplatform project** providing REST API services for project task management. It implements a Gantt chart backend with task scheduling, dependency management, and work calendar support.

## Boundaries

### Included
- Ktor HTTP server with REST API endpoints
- Domain models for tasks, schedules, and dependencies
- Repository pattern with multiple implementations
- Work calendar service for working day calculations
- Data transfer objects (DTOs) for API communication
- Domain↔Transport mapping layer

### Excluded
- Frontend UI (handled by Next.js application)
- Database schema migrations (managed separately)
- Authentication/authorization (not yet implemented)

## Key Concepts

| Concept | Description |
|---------|-------------|
| **Project Task** | A task in the project plan with title, description, duration, and status |
| **Task Schedule** | Scheduled start and end dates for a task |
| **Task Dependency** | Relationship between tasks (predecessor → successor) |
| **Work Calendar** | Defines working days, weekends, and holidays |
| **Schedule Delta** | Changes to schedules after task modifications |
| **Project Plan** | Collection of tasks with schedules and dependencies |

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│           HTTP Layer (Ktor)             │
│  Routing, Serialization, Status Pages   │
├─────────────────────────────────────────┤
│         Transport Layer (DTOs)          │
│  Request/Response data transfer objects │
├─────────────────────────────────────────┤
│           Mappers Layer                 │
│  Domain ↔ Transport conversion          │
├─────────────────────────────────────────┤
│          Domain Layer (Common)          │
│  Models, Repository interfaces          │
├─────────────────────────────────────────┤
│      Infrastructure Layer (Repo)        │
│  PostgreSQL, In-Memory implementations  │
└─────────────────────────────────────────┘
```

### Module Dependencies

```
ktor-app
├── common (domain models)
├── transport (DTOs)
├── mappers (conversions)
├── calendar-service (work calendar)
└── repo implementations
    ├── postgres
    └── in-memory
```

## External Connections

| Connection | Type | Description |
|------------|------|-------------|
| **PostgreSQL** | Database | Persistent storage via atlas-project-backend-postgres |
| **Frontend** | HTTP Client | Next.js application consuming REST API |

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Kotlin | (multiplatform) |
| HTTP Server | Ktor | Latest |
| Build Tool | Gradle Kotlin DSL | Latest |
| Database | PostgreSQL | Via JDBC |
| Date/Time | kotlinx-datetime | Latest |
| Serialization | kotlinx.serialization | Latest |

## Module Structure

| Module | Purpose |
|--------|---------|
| `atlas-project-backend-ktor-app` | HTTP server, routing, configuration |
| `atlas-project-backend-common` | Domain models, repository interfaces |
| `atlas-project-backend-transport` | DTOs, commands, data transfer |
| `atlas-project-backend-mappers` | Domain ↔ Transport mapping |
| `atlas-project-backend-calendar-service` | Work calendar logic |
| `atlas-project-backend-postgres` | PostgreSQL repository |
| `atlas-project-backend-repo-in-memory` | In-memory repository (testing) |

## Navigation

- [Index](INDEX.md) - Complete module structure
- [Details](details/) - In-depth documentation for each module

---

## Quick Reference

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/work-calendar` | GET | Get work calendar |
| `/project-plan` | GET | Get complete project plan |
| `/change-start` | POST | Change task start date |
| `/change-end` | POST | Change task end date |
| `/dependencies` | POST | Create task dependency |
| `/dependencies/recalculate` | POST | Recalculate dependent tasks |
| `/project-tasks` | POST | Create new task |
| `/project-tasks/:id` | PATCH | Update task |

### Domain Models

- `ProjectTask` - Task entity
- `TaskSchedule` - Task schedule with dates
- `TaskDependency` - Dependency between tasks
- `ProjectPlan` - Complete project plan
- `TimelineCalendar` - Work calendar
