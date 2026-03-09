# Frontend Types

**Path:** `/frontend/atlas-project-web-app/src/types`  
**Last Updated:** 2026-03-09

## Overview

TypeScript types and Zod validation schemas for type-safe data handling. Types are organized into schemas (Zod), type definitions, and auto-generated types from the backend.

## Structure

```
types/
├── index.ts                    # Main barrel export
├── schemas/                    # Zod validation schemas
│   ├── task.schema.ts          # Task-related schemas
│   ├── timeline-calendar.schema.ts # Calendar schemas
│   ├── gantt-project-plan.schema.ts # Project plan schemas
│   ├── schedule-delta.schema.ts # Schedule change schemas
│   └── critical-path.schema.ts # CPM schemas
├── types/                      # TypeScript type definitions
│   ├── TaskCommand.type.ts     # Command pattern types
│   ├── CreateTaskCommand.type.ts
│   ├── TaskId.type.ts
│   └── LocalDate.type.ts
└── generated/                  # Auto-generated from Kotlin
    └── enums/
        └── project-task-status.enum.ts
```

## Main Exports

### Task Types

```typescript
// From task.schema.ts
export type Task
export type ScheduledTask
export type GanttTask

export const TaskSchema
export const ScheduledTaskSchema
export const GanttTaskSchema
export const TaskListSchema
export const ScheduledTaskListSchema
export const GanttTaskListSchema
```

### Calendar Types

```typescript
// From timeline-calendar.schema.ts
export type TimelineCalendar
export type DayOfWeek

export const TimelineCalendarSchema
export const DayOfWeekSchema
export const createDefaultTimelineCalendar
```

### Project Plan Types

```typescript
// From gantt-project-plan.schema.ts
export type GanttProjectPlan
export type GanttDependencyDto

export const GanttProjectPlanSchema
```

### Schedule Delta Types

```typescript
// From schedule-delta.schema.ts
export type ScheduleDelta

export const ScheduleDeltaSchema
```

### Critical Path Types

```typescript
// From critical-path.schema.ts
export type CriticalPath
export type CpmTask

export const CriticalPathSchema
```

### Command Types

```typescript
// From types/
export type TaskCommand
export type CreateTaskCommand
export type TaskId
export type LocalDate
```

## Zod Schemas

### Task Schema

```typescript
export const TaskSchema = z.object({
  id: z.string(),
  title: z.string(),
  description: z.string(),
  start: z.string().nullable(),
  end: z.string().nullable(),
  status: ProjectTaskStatusSchema,
})
```

### Timeline Calendar Schema

```typescript
export const TimelineCalendarSchema = z.object({
  id: z.string(),
  name: z.string(),
  workingWeek: z.array(DayOfWeekSchema),
  holidays: z.array(z.object({
    date: z.string(),
    name: z.string(),
  })),
  workingWeekends: z.array(z.string()),
})
```

### Gantt Project Plan Schema

```typescript
export const GanttProjectPlanSchema = z.object({
  projectId: z.string(),
  tasks: GanttTaskListSchema,
  dependencies: z.array(GanttDependencyDtoSchema),
})
```

### Schedule Delta Schema

```typescript
export const ScheduleDeltaSchema = z.object({
  updatedSchedules: z.array(z.object({
    taskId: z.string(),
    start: z.string(),
    end: z.string(),
  })),
})
```

### Critical Path Schema

```typescript
export const CriticalPathSchema = z.object({
  criticalTaskIds: z.array(z.string()),
  tasks: z.array(z.object({
    taskId: z.string(),
    slack: z.number(),
  })),
})
```

## Type Definitions

### TaskCommand

Union type for all task operations:

```typescript
export type TaskCommand =
  | { type: TaskCommandType.CreateTaskInPool; title: string }
  | { type: TaskCommandType.ChangeStartDate; taskId: string; newStartDate: string }
  | { type: TaskCommandType.ChangeEndDate; taskId: string; newEndDate: string }
  | { type: TaskCommandType.UpdateTitle; taskId: string; newTitle: string }
  | { type: TaskCommandType.UpdateDescription; taskId: string; newDescription: string }
  | { type: TaskCommandType.ChangeStatus; taskId: string; newStatus: ProjectTaskStatus }
  | { type: TaskCommandType.DeleteTask; taskId: string }
  | { type: TaskCommandType.PlanFromEnd; taskId: string; newEndDate: string }
  | { type: TaskCommandType.AssignSchedule; taskId: string; start: string; duration: number }
  | { type: TaskCommandType.MoveTask; taskId: string; newStartDate: string }
  | { type: TaskCommandType.ResizeTask; taskId: string; newEndDate: string }
```

### TaskCommandType

Enum for command types:

```typescript
export enum TaskCommandType {
  CreateTaskInPool,
  ChangeStartDate,
  ChangeEndDate,
  UpdateTitle,
  UpdateDescription,
  ChangeStatus,
  DeleteTask,
  PlanFromEnd,
  AssignSchedule,
  MoveTask,
  ResizeTask,
}
```

### Utility Types

```typescript
// TaskId type constructor
export type TaskId = string & { readonly __brand: unique symbol }
export const TaskId = (value: string): TaskId => value as TaskId

// LocalDate type constructor
export type LocalDate = string & { readonly __brand: unique symbol }
export const LocalDate = (value: string): LocalDate => value as LocalDate
```

## Generated Types

Types in the `generated/` directory are auto-generated from Kotlin backend DTOs using the `kxs-ts-gen` library.

### Generation Command

Run from the backend directory:

```bash
./gradlew :atlas-project-backend-transport:generateTypeScript
```

### Generated Files

- `enums/project-task-status.enum.ts` - Project task status enum

## Usage

### Parsing API Responses

```typescript
import { TaskSchema, GanttProjectPlanSchema } from '@/types'

const response = await fetch('/api/project-tasks')
const tasks = TaskSchema.parse(await response.json())

const planResponse = await fetch('/api/project-plan')
const plan = GanttProjectPlanSchema.parse(await planResponse.json())
```

### Using Commands

```typescript
import { TaskCommandType, TaskId, LocalDate } from '@/types'

const command: TaskCommand = {
  type: TaskCommandType.ChangeStartDate,
  taskId: TaskId('task-123'),
  newStartDate: LocalDate('2026-03-15'),
}
```

## Related Files

- [Services](../services/README.md) - API services using these types
- [Hooks](../hooks/README.md) - React Query hooks with typed responses
