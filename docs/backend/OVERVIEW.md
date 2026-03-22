# Backend - Overview

**Path:** `/backend/atlas-project-backend`  
**Parent:** [Project Documentation Map](../PROJECT_MAP.md)  
**Last Updated:** 2026-03-03

## Purpose

The backend is a **Kotlin multiplatform project** providing REST API services for project task management. It implements a Gantt chart backend with task scheduling, dependency management, work calendar support, resource management, and resource leveling.

## Boundaries

### Included
- Ktor HTTP server with REST API endpoints
- Domain models for tasks, schedules, dependencies, resources, and assignments
- Repository pattern with multiple implementations
- Work calendar service for working day calculations
- Resource management (create, update, delete resources)
- Task assignments (assign resources to tasks)
- Resource load calculation and overload detection
- Resource leveling engine for automatic overload resolution
- Critical path analysis
- Project analysis (blocker chain, what-if scenarios)
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
| **Resource** | A person or role with capacity (hours/day) available for work |
| **Task Assignment** | Link between a resource and task with hours/day allocation |
| **Resource Load** | Calculation of assigned vs. available hours per day |
| **Resource Leveling** | Automatic rescheduling to resolve resource overloads |
| **Critical Path** | Longest path through the project network (zero slack tasks) |
| **Blocker Chain** | Chain of blocking tasks preventing a task from starting earlier |

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
| Language | Kotlin | 2.3.10 |
| HTTP Server | Ktor | 3.4.0 |
| Build Tool | Gradle Kotlin DSL | Latest |
| Database | PostgreSQL | 42.7.9 (JDBC driver) |
| ORM | Exposed | 0.61.0 |
| Migrations | Flyway | 10.20.1 |
| Date/Time | kotlinx-datetime | 0.7.1 |
| Serialization | kotlinx.serialization | 1.10.0 |
| Coroutines | kotlinx-coroutines | 1.10.2 |

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

#### Task Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/work-calendar` | GET | Get work calendar |
| `/project-plan` | GET | Get complete project plan |
| `/change-start` | POST | Change task start date |
| `/change-end` | POST | Change task end date |
| `/resize-from-start` | POST | Resize task from start (keep end fixed) |
| `/plan-from-end` | POST | Plan task backwards from end date |
| `/dependencies` | POST | Create task dependency |
| `/dependencies` | PATCH | Change dependency type |
| `/dependencies` | DELETE | Remove dependency |
| `/dependencies/recalculate` | POST | Recalculate dependent tasks |
| `/project-tasks` | POST | Create new task |
| `/project-tasks/create-in-pool` | POST | Create task without schedule |
| `/project-tasks/:id` | PATCH | Update task |
| `/project-tasks/:id` | DELETE | Delete task |
| `/project-tasks/:id/schedule` | POST | Assign schedule to pool task |

#### Resource Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/resources` | GET | List all resources |
| `/resources` | POST | Create new resource |
| `/resources/:id` | PATCH | Update resource |
| `/resources/:id` | DELETE | Delete resource |
| `/resources/:id/calendar-overrides` | GET/POST/DELETE | Manage calendar overrides |

#### Assignment Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/assignments` | GET | List all assignments |
| `/assignments` | POST | Create assignment |
| `/assignments/:id` | PATCH | Update assignment |
| `/assignments/:id` | DELETE | Delete assignment |
| `/assignments/:id/day-overrides` | GET/POST/DELETE | Manage day overrides |

#### Resource Load & Leveling

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/resource-load` | GET | Get resource overload report |
| `/resource-load/:resourceId` | GET | Get single resource load |
| `/leveling/preview` | POST | Preview leveling result |
| `/leveling/apply` | POST | Apply leveling changes |

#### Analysis

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/critical-path` | GET | Critical path analysis |
| `/analysis/blocker-chain/:taskId` | GET | Blocker chain for task |
| `/analysis/available-tasks` | GET | Available tasks from date |
| `/analysis/what-if` | GET | What-if analysis (start change) |
| `/analysis/what-if-end` | GET | What-if analysis (end change) |

### Domain Models

#### Task Models
- `ProjectTask` - Task entity
- `TaskSchedule` - Task schedule with dates
- `TaskDependency` - Dependency between tasks
- `ProjectPlan` - Complete project plan
- `TimelineCalendar` - Work calendar

#### Resource Models
- `Resource` - Person or role with capacity
- `TaskAssignment` - Resource-to-task assignment
- `ResourceCalendarOverride` - Daily capacity override
- `AssignmentDayOverride` - Assignment-specific day override
