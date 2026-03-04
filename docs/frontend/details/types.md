# Types - Detail

**Path:** `/frontend/atlas-project-web-app/src/types/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-04

## Purpose

Type definitions, Zod validation schemas, and DTOs for type-safe data handling throughout the application. Provides a single source of truth for data shapes.

## Directory Structure

```
types/
├── index.ts                    # Barrel exports
├── enums/                      # Enum definitions
│   ├── duration-unit.enum.ts
│   └── task-status.enum.ts
├── generated/                  # Auto-generated DTOs from backend
│   ├── calendar/
│   ├── commands/
│   ├── enums/
│   ├── ganttProjectPlan/
│   ├── plan/
│   ├── timelineCalendar/
│   ├── *.dto.ts
│   └── index.ts
├── interfaces/                 # Interface definitions
├── schemas/                    # Zod validation schemas
│   ├── gantt-project-plan.schema.ts
│   ├── schedule-delta.schema.ts
│   ├── task.schema.ts
│   └── timeline-calendar.schema.ts
├── types/                      # Type aliases
│   ├── ChangeEndDateCommand.type.ts
│   ├── ChangeStartDateCommand.type.ts
│   ├── CreateTaskCommand.type.ts
│   ├── CreateTaskInPoolCommand.type.ts
│   ├── LocalDate.type.ts
│   ├── TaskCommand.type.ts
│   ├── TaskCommandType.ts
│   ├── TaskId.type.ts
│   └── UpdateTitleCommand.type.ts
└── index.ts
```

---

## Generated DTOs (`types/generated/`)

### Purpose

TypeScript DTOs that mirror the backend Kotlin data classes. These are typically auto-generated from the backend's transport layer.

### Key DTOs

| File | Type | Description |
|------|------|-------------|
| `task.dto.ts` | `TaskDto` | Basic task data |
| `gantt-task.dto.ts` | `GanttTaskDto` | Task with schedule info |
| `gantt-dependency.dto.ts` | `GanttDependencyDto` | Task dependency |
| `scheduled-task.dto.ts` | `ScheduledTaskDto` | Task with dates |
| `work-calendar.dto.ts` | `WorkCalendarDto` | Calendar configuration |
| `timeline-calendar.dto.ts` | `TimelineCalendarDto` | Timeline calendar |
| `schedule-delta.dto.ts` | `ScheduleDeltaDto` | Schedule changes |
| `create-project-task.dto.ts` | `CreateProjectTaskDto` | Task creation request |
| `update-project-task.dto.ts` | `UpdateProjectTaskDto` | Task update request |

### Command DTOs

| File | Type | Description |
|------|------|-------------|
| `commands/change-task-end-date-command.dto.ts` | `ChangeTaskEndDateCommandDto` | End date change |
| `commands/change-task-start-date-command.dto.ts` | `ChangeTaskStartDateCommandDto` | Start date change |
| `commands/create-dependency-command.dto.ts` | `CreateDependencyCommandDto` | Dependency creation |
| `commands/create-task-in-pool-command.dto.ts` | `CreateTaskInPoolCommandDto` | Pool task creation |

---

## Schemas (`types/schemas/`)

### Purpose

Zod schemas for runtime validation of API responses and user input.

### task.schema.ts

```typescript
import { z } from 'zod'

export const TaskSchema = z.object({
    id: z.string(),
    title: z.string(),
    description: z.string(),
    duration: z.number(),
    status: z.enum(['EMPTY', 'IN_PROGRESS', 'DONE']),
})

export const TaskListSchema = z.array(TaskSchema)

export type Task = z.infer<typeof TaskSchema>
export type TaskList = z.infer<typeof TaskListSchema>
```

---

### gantt-project-plan.schema.ts

```typescript
import { z } from 'zod'
import { GanttTaskSchema } from './gantt-task.schema'
import { GanttDependencySchema } from './gantt-dependency.schema'

export const GanttProjectPlanSchema = z.object({
    projectId: z.string(),
    tasks: z.array(GanttTaskSchema),
    dependencies: z.array(GanttDependencySchema),
})

export type GanttProjectPlan = z.infer<typeof GanttProjectPlanSchema>
```

---

### schedule-delta.schema.ts

```typescript
import { z } from 'zod'

export const ScheduleDeltaSchema = z.object({
    updatedSchedule: z.array(z.object({
        id: z.string(),
        start: z.string(),
        end: z.string(),
    })),
})

export type ScheduleDelta = z.infer<typeof ScheduleDeltaSchema>
```

---

### timeline-calendar.schema.ts

```typescript
import { z } from 'zod'

export const TimelineCalendarSchema = z.object({
    holidays: z.array(z.string()),
    workingDays: z.array(z.number()),
    // ... calendar configuration
})

export type TimelineCalendar = z.infer<typeof TimelineCalendarSchema>
```

---

## Type Aliases (`types/types/`)

### TaskCommand Types

Defines the command pattern for task operations:

```typescript
// TaskCommandType.ts
export const TaskCommandType = {
    CreateTaskInPool = 'createTaskInPool',
    ChangeStartDate = 'changeStartDate',
    ChangeEndDate = 'changeEndDate',
    UpdateTitle = 'updateTitle',
    DeleteTask = 'deleteTask',
} as const

// TaskCommand.type.ts - Union type
export type TaskCommand =
    | CreateTaskInPoolCommand
    | ChangeStartDateCommand
    | ChangeEndDateCommand
    | UpdateTitleCommand
    | DeleteTaskCommand

// Individual command types
export type CreateTaskInPoolCommand = {
    type: typeof TaskCommandType.CreateTaskInPool
    title: string
}

export type ChangeStartDateCommand = {
    type: typeof TaskCommandType.ChangeStartDate
    taskId: string
    newStartDate: string
}

export type ChangeEndDateCommand = {
    type: typeof TaskCommandType.ChangeEndDate
    taskId: string
    newEndDate: string
}

export type UpdateTitleCommand = {
    type: typeof TaskCommandType.UpdateTitle
    taskId: string
    newTitle: string
}

export type DeleteTaskCommand = {
    type: typeof TaskCommandType.DeleteTask
    taskId: string
}
```

---

### Utility Types

```typescript
// TaskId.type.ts
export type TaskId = string

// LocalDate.type.ts
export type LocalDate = string  // ISO 8601 format
```

---

## Enums (`types/enums/`)

### task-status.enum.ts

```typescript
export enum TaskStatus {
    EMPTY = 'EMPTY',
    IN_PROGRESS = 'IN_PROGRESS',
    DONE = 'DONE',
}
```

---

### duration-unit.enum.ts

```typescript
export enum DurationUnit {
    DAYS = 'DAYS',
    WEEKS = 'WEEKS',
    MONTHS = 'MONTHS',
}
```

---

## Main Export (index.ts)

```typescript
// Re-export all types and schemas
export * from './schemas/task.schema'
export * from './schemas/gantt-project-plan.schema'
export * from './schemas/schedule-delta.schema'
export * from './schemas/timeline-calendar.schema'

export * from './types/TaskCommand.type'
export * from './types/TaskCommandType'
export * from './types/TaskId.type'

export * from './enums/task-status.enum'
export * from './enums/duration-unit.enum'

export * from './generated'
```

---

## Dependencies

**Imports:**
- `z` from `zod`

**Imported by:**
- All components, hooks, and services
- API service layer for response validation

---

## Related Files

- [Services](./services.md)
- [Hooks](./hooks.md)
- [Backend Transport](../../backend/details/transport.md)
