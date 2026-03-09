# Atlas Project Web App

**Path:** `/frontend/atlas-project-web-app`  
**Last Updated:** 2026-03-09

## Overview

A modern React-based Gantt chart application for project planning and task management. Built with Next.js 16, TypeScript, and Tailwind CSS, this frontend provides an interactive timeline visualization for project tasks with drag-and-drop scheduling, dependency management, and critical path analysis.

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Framework** | Next.js | 16.1.6 |
| **UI Library** | React | 19.2.3 |
| **Language** | TypeScript | 5.x |
| **Styling** | Tailwind CSS | 4.x |
| **State Management** | Zustand | 5.0.11 |
| **Data Fetching** | TanStack Query (React Query) | 5.90.21 |
| **Drag & Drop** | @dnd-kit/react | 0.3.2 |
| **UI Components** | PrimeReact | 10.9.7 |
| **Icons** | Lucide React | 0.575.0 |
| **Validation** | Zod | 4.3.6 |
| **Utilities** | tailwind-merge, tailwind-variants | 3.x |

## Project Structure

```
atlas-project-web-app/
├── src/
│   ├── app/                    # Next.js App Router
│   │   ├── layout.tsx          # Root layout with providers
│   │   ├── page.tsx            # Home page (GanttChart)
│   │   └── globals.css         # Global styles
│   ├── components/             # React components
│   │   ├── GanttChart/         # Main Gantt chart components
│   │   ├── Task/               # Task display components
│   │   └── *.tsx               # Shared components
│   ├── hooks/                  # Custom React hooks
│   │   ├── useProjectTasks.ts  # Project task mutations/queries
│   │   └── useTimelineCalendar.ts # Calendar state hook
│   ├── services/               # API service layer
│   │   ├── projectTasksApi.ts  # Task API calls
│   │   └── timelineCalendarApi.ts # Calendar API calls
│   ├── store/                  # Zustand stores
│   │   └── timelineCalendarStore.ts # UI state store
│   ├── types/                  # TypeScript types & Zod schemas
│   │   ├── index.ts            # Type exports
│   │   ├── schemas/            # Zod validation schemas
│   │   ├── types/              # TypeScript type definitions
│   │   └── generated/          # Auto-generated types from backend
│   ├── utils/                  # Utility functions
│   │   └── ganttDateUtils.ts   # Date calculations for Gantt
│   ├── providers/              # React context providers
│   │   └── QueryProvider.tsx   # TanStack Query provider
│   └── constants/              # Application constants
├── public/                     # Static assets
├── package.json                # Dependencies & scripts
├── tsconfig.json               # TypeScript configuration
├── next.config.ts              # Next.js configuration
├── eslint.config.mjs           # ESLint configuration
├── postcss.config.mjs          # PostCSS configuration
└── README.md                   # This file
```

## Core Features

### Gantt Chart Visualization
- **Interactive Timeline**: Day and week view modes
- **Task Management**: Create, edit, delete tasks
- **Drag & Drop**: Move tasks on timeline, resize from start/end
- **Dependencies**: Create and manage task dependencies (FS, SS, FF, SF)
- **Critical Path**: Visual highlighting of critical path tasks
- **Calendar Awareness**: Working days, weekends, holidays support

### Task Operations
| Operation | Description |
|-----------|-------------|
| Create Task | Add new tasks to the task pool |
| Schedule Task | Drag from pool to timeline to assign dates |
| Move Task | Drag existing tasks to new dates |
| Resize Task | Drag task edges to change duration |
| Edit Details | Update title, description, status |
| Delete Task | Remove tasks (with dependency warnings) |
| Plan from End | Schedule backward from end date |

### Dependency Management
- **Dependency Types**: Finish-Start (FS), Start-Start (SS), Finish-Finish (FF), Start-Finish (SF)
- **Visual Links**: Dependency lines between tasks
- **Context Menu**: Change type or remove dependencies via popover
- **Validation**: Prevent circular dependencies

## Getting Started

### Prerequisites
- Node.js 20.x or later
- npm or yarn

### Installation

```bash
cd frontend/atlas-project-web-app
npm install
```

### Development

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Start production server
npm run start

# Run linter
npm run lint
```

### Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Architecture

### State Management

**Zustand Store** (`timelineCalendarStore.ts`):
- UI state (view mode, show holidays/weekends, settings panel)
- Actions: `setViewMode`, `toggleHolidays`, `toggleWeekends`, `setSettingsPanelOpen`

**TanStack Query** (`useProjectTasks.ts`, `useTimelineCalendar.ts`):
- Server state management
- Optimistic updates for responsive UI
- Automatic refetching and cache invalidation

### Component Hierarchy

```
GanttChart (main container)
├── GanttTaskList (left panel)
│   └── Task (individual task rows)
├── GanttCalendarHeader (top timeline)
│   └── Month/week headers
└── GanttCalendarGrid (main timeline area)
    ├── GanttTaskRow (task bars)
    ├── GanttDependencyLines (SVG connections)
    └── Drop zones (for scheduling)
```

### API Integration

**Services Layer** (`services/projectTasksApi.ts`):
- Fetch-based API client
- Zod schema validation for responses
- Error handling

**API Endpoints**:
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/project-tasks` | GET | Get all tasks |
| `/project-tasks` | POST | Create task |
| `/project-tasks/:id` | PATCH | Update task |
| `/project-tasks/:id` | DELETE | Delete task |
| `/project-plan` | GET | Get full project plan |
| `/change-start` | POST | Change task start date |
| `/change-end` | POST | Change task end date |
| `/resize-from-start` | POST | Resize task from start |
| `/dependencies` | POST/DELETE/PATCH | Manage dependencies |
| `/critical-path` | GET | Get critical path analysis |

### Type Generation

TypeScript types are auto-generated from Kotlin backend DTOs:

```bash
# From backend directory
./gradlew :atlas-project-backend-transport:generateTypeScript
```

Generated types are placed in `src/types/generated/`.

## Key Conventions

### File Naming
- Components: PascalCase (e.g., `GanttChart.tsx`)
- Hooks: camelCase with `use` prefix (e.g., `useProjectTasks.ts`)
- Services: camelCase with `Api` suffix (e.g., `projectTasksApi.ts`)
- Types: camelCase with `.schema.ts` or `.type.ts` suffix

### Component Pattern
```tsx
'use client'  // For interactive components

import { useCallback } from 'react'

export const ComponentName = ({ prop }: Props) => {
  // Hooks first
  // Handlers with useCallback
  // Render
}
```

### Command Pattern

Task operations use a command pattern for type-safe updates:

```typescript
type TaskCommand = 
  | { type: TaskCommandType.CreateTaskInPool; title: string }
  | { type: TaskCommandType.ChangeStartDate; taskId: string; newStartDate: string }
  | { type: TaskCommandType.MoveTask; taskId: string; newStartDate: string }
  // ... etc
```

## Related Documentation

- [Backend Documentation](../../backend/atlas-project-backend/README.md)
- [Project Overview](../../docs/project/)

## Notes

- **Next.js 16**: Uses the new App Router with Server Components
- **React 19**: Leverages latest React features
- **Tailwind CSS 4**: Uses the new Tailwind engine
- **Strict TypeScript**: All code is strictly typed with no `any`
