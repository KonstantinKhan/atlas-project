# Frontend - Index

**Path:** `/frontend/atlas-project-web-app`  
**Parent:** [Project Documentation Map](../PROJECT_MAP.md)  
**Last Updated:** 2026-03-03

## Directory Structure

```
src/
├── app/                      # Next.js app router
│   ├── layout.tsx            # Root layout
│   ├── page.tsx              # Home page (GanttChart)
│   ├── globals.css           # Global styles
│   └── favicon.ico
├── components/               # React components
│   ├── GanttChart/           # Gantt chart components (9 files)
│   ├── Task/                 # Task components
│   ├── Block.tsx             # Base block component
│   ├── DraggableBlock.tsx    # Draggable wrapper
│   ├── DroppableBlock.tsx    # Droppable wrapper
│   ├── GantElement.tsx       # Gantt element
│   ├── MyBlock.tsx           # Custom block
│   └── DefaultZone.tsx       # Default drop zone
├── hooks/                    # Custom React hooks
│   ├── useProjectTasks.ts    # Task API hooks
│   └── useTimelineCalendar.ts # Calendar API hook
├── services/                 # API service layer
│   ├── projectTasksApi.ts    # Task CRUD operations
│   └── timelineCalendarApi.ts # Calendar operations
├── store/                    # Zustand state management
│   └── timelineCalendarStore.ts # UI state store
├── types/                    # TypeScript types
│   ├── enums/                # Enum definitions
│   ├── generated/            # Auto-generated DTOs
│   ├── interfaces/           # Interface definitions
│   ├── schemas/              # Zod validation schemas
│   ├── types/                # Type aliases
│   └── index.ts              # Type exports
├── utils/                    # Utility functions
│   ├── types/                # Utility types
│   ├── formatDate.ts         # Date formatting
│   ├── ganttDateUtils.ts     # Gantt date calculations
│   ├── ganttDependencyUtils.ts # Dependency utilities
│   └── index.ts (if exists)
├── constants/                # Application constants
└── providers/                # React providers
    └── QueryProvider.tsx     # TanStack Query provider
```

## Component Modules

| Module | Path | Description | Links |
|--------|------|-------------|-------|
| **GanttChart** | `./components/GanttChart/` | Main Gantt chart visualization with calendar grid, task rows, and dependency lines | [Overview](details/components-gantt-chart.md) |
| **Task** | `./components/Task/` | Task row component with inline editing | [Overview](details/components-task.md) |
| **Blocks** | `./components/` | Drag-and-drop block components | [Overview](details/components-blocks.md) |

## Hook Modules

| Module | Path | Description | Links |
|--------|------|-------------|-------|
| **useProjectTasks** | `./hooks/useProjectTasks.ts` | React Query hooks for task CRUD operations | [Detail](details/hooks.md#useprojecttasks) |
| **useTimelineCalendar** | `./hooks/useTimelineCalendar.ts` | React Query hook for work calendar | [Detail](details/hooks.md#usetimelinecalendar) |

## Service Modules

| Module | Path | Description | Links |
|--------|------|-------------|-------|
| **projectTasksApi** | `./services/projectTasksApi.ts` | API service for task operations | [Detail](details/services.md#projecttasksapits) |
| **timelineCalendarApi** | `./services/timelineCalendarApi.ts` | API service for calendar operations | [Detail](details/services.md#timelinecalendarapits) |

## State Management

| Module | Path | Description | Links |
|--------|------|-------------|-------|
| **timelineCalendarStore** | `./store/timelineCalendarStore.ts` | Zustand store for UI state | [Detail](details/store.md) |

## Type Definitions

| Module | Path | Description | Links |
|--------|------|-------------|-------|
| **Generated DTOs** | `./types/generated/` | Auto-generated TypeScript DTOs from backend | [Detail](details/types.md#generated-dtos) |
| **Schemas** | `./types/schemas/` | Zod validation schemas | [Detail](details/types.md#schemas) |
| **Enums** | `./types/enums/` | TypeScript enum definitions | [Detail](details/types.md#enums) |
| **Type Aliases** | `./types/types/` | Custom type aliases | [Detail](details/types.md#type-aliases) |

## Dependencies

### Internal Dependencies
```
GanttChart → Task, Hooks, Services, Store, Types, Utils
Task → Types, Utils
Hooks → Services, Types
Services → Types
Store → (none)
Utils → Types
```

### External Dependencies (package.json)
| Package | Version | Purpose |
|---------|---------|---------|
| next | 16.1.6 | React framework |
| react | 19.2.3 | UI library |
| @dnd-kit/react | 0.3.2 | Drag-and-drop |
| @tanstack/react-query | 5.90.21 | Data fetching |
| zustand | 5.0.11 | State management |
| zod | 4.3.6 | Schema validation |
| tailwindcss | 4.x | Styling |
| primereact | 10.9.7 | UI components |
| lucide-react | 0.575.0 | Icons |

## API Endpoints Consumed

| Endpoint | Method | Hook/Service | Description |
|----------|--------|--------------|-------------|
| `/work-calendar` | GET | useTimelineCalendar | Fetch work calendar |
| `/project-plan` | GET | useProjectPlan | Fetch project plan |
| `/change-start` | POST | changeTaskStartDate | Update task start (drag bar) |
| `/change-end` | POST | changeTaskEndDate | Update task end (resize right) |
| `/resize-from-start` | POST | resizeTaskFromStart | Resize from start (drag left edge) |
| `/dependencies` | POST | createDependency | Create dependency |
| `/dependencies` | PATCH | changeDependencyType | Change dependency type |
| `/dependencies` | DELETE | deleteDependency | Delete dependency |
| `/dependencies/recalculate` | POST | recalculateDependencies | Recalculate all dependencies |
| `/project-tasks` | POST | createProjectTask | Create task |
| `/project-tasks/:id` | PATCH | updateProjectTask | Update task |
| `/project-tasks/:id` | DELETE | deleteProjectTask | Delete task |
| `/project-tasks/:id/schedule` | POST | assignTaskSchedule | Assign schedule to pool task |
| `/plan-from-end` | POST | planTaskFromEnd | Plan backwards from end date |

---

## Navigation

- [Back to Overview](OVERVIEW.md)
- [Details Directory](details/)
- [Project Map](../PROJECT_MAP.md)
