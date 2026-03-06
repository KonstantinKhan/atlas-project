# Project Documentation Map

**Project:** Atlas Project  
**Version:** 0.0.1  
**Last Updated:** 2026-03-03

## Overview

Atlas Project is a **Gantt chart-based project management application** with a Kotlin backend and Next.js frontend. It enables users to visualize project timelines, manage task schedules, and handle task dependencies through an interactive interface.

## Quick Navigation

| Module       | Overview                         | Index                      | Details                      |
| ------------ | -------------------------------- | -------------------------- | ---------------------------- |
| **Frontend** | [Overview](frontend/OVERVIEW.md) | [Index](frontend/INDEX.md) | [Details](frontend/details/) |
| **Backend**  | [Overview](backend/OVERVIEW.md)  | [Index](backend/INDEX.md)  | [Details](backend/details/)  |

---

## Frontend Documentation

**Path:** `/frontend/atlas-project-web-app`  
**Technology:** Next.js 16, React 19, TypeScript

### Structure

```
docs/frontend/
├── OVERVIEW.md           # High-level architecture and tech stack
├── INDEX.md              # Complete file listing and dependencies
└── details/
    ├── components-gantt-chart.md   # GanttChart module (9 files)
    ├── components-task.md          # Task component
    ├── components-blocks.md        # Drag-and-drop blocks
    ├── hooks.md                    # React Query hooks
    ├── services.md                 # API service layer
    ├── store.md                    # Zustand state management
    └── types.md                    # TypeScript types and schemas
```

### Key Components

| Component  | Description                  | Link                                                 |
| ---------- | ---------------------------- | ---------------------------------------------------- |
| GanttChart | Main visualization component | [Detail](frontend/details/components-gantt-chart.md) |
| Task       | Task row with inline editing | [Detail](frontend/details/components-task.md)        |
| Blocks     | Drag-and-drop components     | [Detail](frontend/details/components-blocks.md)      |

### State Management

| Module | Purpose                      | Link                                |
| ------ | ---------------------------- | ----------------------------------- |
| Hooks  | React Query for server state | [Detail](frontend/details/hooks.md) |
| Store  | Zustand for UI state         | [Detail](frontend/details/store.md) |

### API Integration

| Module   | Purpose          | Link                                   |
| -------- | ---------------- | -------------------------------------- |
| Services | API functions    | [Detail](frontend/details/services.md) |
| Types    | DTOs and schemas | [Detail](frontend/details/types.md)    |

---

## Backend Documentation

**Path:** `/backend/atlas-project-backend`  
**Technology:** Kotlin, Ktor, PostgreSQL

### Structure

```
docs/backend/
├── OVERVIEW.md           # High-level architecture and modules
├── INDEX.md              # Complete module listing and APIs
└── details/
    ├── ktor-app.md               # HTTP server and routing
    ├── common.md                 # Domain models
    ├── transport.md              # DTOs
    ├── mappers.md                # Domain↔Transport mapping
    ├── calendar-service.md       # Work calendar logic
    ├── postgres.md               # PostgreSQL repository
    └── repo-in-memory.md         # In-memory repository
```

### Modules

| Module           | Purpose                        | Link                                          |
| ---------------- | ------------------------------ | --------------------------------------------- |
| Ktor App         | HTTP server, REST API          | [Detail](backend/details/ktor-app.md)         |
| Common           | Domain models, repo interfaces | [Detail](backend/details/common.md)           |
| Transport        | Data transfer objects          | [Detail](backend/details/transport.md)        |
| Mappers          | Domain↔DTO conversion          | [Detail](backend/details/mappers.md)          |
| Calendar Service | Working day calculations       | [Detail](backend/details/calendar-service.md) |
| Postgres         | PostgreSQL implementation      | [Detail](backend/details/postgres.md)         |
| Repo In-Memory   | Testing/development            | [Detail](backend/details/repo-in-memory.md)   |

### API Endpoints

| Endpoint                        | Method | Description                  | Link                                  |
| ------------------------------- | ------ | ---------------------------- | ------------------------------------- |
| `/work-calendar`                | GET    | Get work calendar            | [Detail](backend/details/ktor-app.md) |
| `/project-plan`                 | GET    | Get project plan             | [Detail](backend/details/ktor-app.md) |
| `/change-start`                 | POST   | Change task start            | [Detail](backend/details/ktor-app.md) |
| `/change-end`                   | POST   | Change task end              | [Detail](backend/details/ktor-app.md) |
| `/resize-from-start`            | POST   | Resize task from start       | [Detail](backend/details/ktor-app.md) |
| `/dependencies`                 | POST   | Create dependency            | [Detail](backend/details/ktor-app.md) |
| `/dependencies`                 | PATCH  | Change dependency type       | [Detail](backend/details/ktor-app.md) |
| `/dependencies`                 | DELETE | Delete dependency            | [Detail](backend/details/ktor-app.md) |
| `/dependencies/recalculate`     | POST   | Recalculate all schedules    | [Detail](backend/details/ktor-app.md) |
| `/project-tasks`                | POST   | Create task                  | [Detail](backend/details/ktor-app.md) |
| `/project-tasks/create-in-pool` | POST   | Create task without schedule | [Detail](backend/details/ktor-app.md) |
| `/project-tasks/:id`            | PATCH  | Update task                  | [Detail](backend/details/ktor-app.md) |
| `/project-tasks/:id`            | DELETE | Delete task                  | [Detail](backend/details/ktor-app.md) |
| `/project-tasks/:id/schedule`   | POST   | Assign schedule to pool task | [Detail](backend/details/ktor-app.md) |
| `/plan-from-end`                | POST   | Plan backwards from end date | [Detail](backend/details/ktor-app.md) |

---

## For Agents

### Navigation Guide

1. **Start Here** - You are at the project map
2. **Choose Module** - Frontend or Backend
3. **Read Overview** - Understand high-level architecture
4. **Check Index** - See complete file/module listing
5. **Drill Down** - Read details for specific files

### No Project Scanning Needed

This documentation provides:
- ✅ Complete file structure
- ✅ Component/module purposes
- ✅ API endpoint documentation
- ✅ Dependency relationships
- ✅ Usage examples

### Updating Documentation

When code changes:
1. Update the relevant `details/*.md` file
2. Update `INDEX.md` if structure changes
3. Update `OVERVIEW.md` if architecture changes
4. Update this `PROJECT_MAP.md` for major changes

---

## Other Documentation

| Directory          | Purpose                        |
| ------------------ | ------------------------------ |
| `docs/project/`    | Project-level documentation    |
| `docs/rules/`      | Development rules and patterns |
| `docs/ROAD_MAP.md` | Project roadmap                |

---

## Quick Reference

### Frontend Tech Stack

| Layer         | Technology            |
| ------------- | --------------------- |
| Framework     | Next.js 16            |
| UI            | React 19              |
| State         | Zustand + React Query |
| Styling       | Tailwind CSS 4        |
| Drag-and-Drop | @dnd-kit              |

### Backend Tech Stack

| Layer    | Technology        |
| -------- | ----------------- |
| Language | Kotlin            |
| HTTP     | Ktor              |
| Database | PostgreSQL        |
| Build    | Gradle Kotlin DSL |

### Project Structure

```
atlas-project/
├── frontend/atlas-project-web-app/   # Next.js application
├── backend/atlas-project-backend/    # Kotlin backend
├── docs/                             # Documentation
├── postgres/                         # Database scripts
└── [config files]
```
