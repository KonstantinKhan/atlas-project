# Frontend - Overview

**Path:** `/frontend/atlas-project-web-app`  
**Parent:** [Project Documentation Map](../PROJECT_MAP.md)  
**Last Updated:** 2026-03-03

## Purpose

The frontend is a **Next.js 16 + React 19 web application** that provides an interactive Gantt chart interface for project task management. It enables users to visualize project timelines, manage task schedules, and handle task dependencies through a drag-and-drop interface.

## Boundaries

### Included
- Gantt chart visualization component with calendar grid
- Task list with inline editing capabilities
- Drag-and-drop task scheduling (@dnd-kit)
- API integration with backend services
- Client-side state management (Zustand, React Query)
- Type-safe data validation (Zod schemas)
- Responsive UI with Tailwind CSS and PrimeReact

### Excluded
- Backend business logic (handled by Kotlin backend)
- Database operations (PostgreSQL via backend)
- Authentication/authorization (not yet implemented)

## Key Concepts

| Concept | Description |
|---------|-------------|
| **Gantt Chart** | Visual timeline showing tasks, durations, and dependencies |
| **Task Commands** | Type-safe command pattern for task operations (create, update, reschedule) |
| **Timeline Calendar** | Work calendar defining working days, weekends, and holidays |
| **Project Plan** | Collection of tasks with schedules and dependencies |
| **Drag-and-Drop** | Interactive task scheduling using @dnd-kit react |

## External Connections

| Connection | Type | Description |
|------------|------|-------------|
| [Backend API](../backend/OVERVIEW.md) | HTTP REST | Ktor server at `http://localhost:8080` |
| **Depends on:** | | |
| - `/work-calendar` | GET | Fetch work calendar configuration |
| - `/project-plan` | GET | Fetch complete project plan with tasks |
| - `/change-start` | POST | Update task start date |
| - `/change-end` | POST | Update task end date |
| - `/dependencies` | POST | Create task dependencies |
| - `/project-tasks` | POST/PATCH | Create/update tasks |

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Framework | Next.js | 16.1.6 |
| UI Library | React | 19.2.3 |
| State Management | Zustand | 5.0.11 |
| Data Fetching | TanStack Query | 5.90.21 |
| Drag-and-Drop | @dnd-kit/react | 0.3.2 |
| Styling | Tailwind CSS | 4.x |
| UI Components | PrimeReact | 10.9.7 |
| Icons | Lucide React | 0.575.0 |
| Validation | Zod | 4.3.6 |
| Language | TypeScript | 5.x |

## Navigation

- [Index](INDEX.md) - Complete module structure and file listing
- [Details](details/) - In-depth documentation for each component/file

---

## Quick Reference

### Main Entry Point
- `src/app/page.tsx` - Renders the GanttChart component

### Core Components
- `components/GanttChart/` - Gantt chart visualization (9 files)
- `components/Task/` - Task row component
- `components/*.tsx` - Block components for drag-and-drop

### State & Data
- `hooks/` - React Query hooks for API integration
- `store/` - Zustand store for UI state
- `services/` - API service layer
- `types/` - TypeScript types, schemas, and DTOs
