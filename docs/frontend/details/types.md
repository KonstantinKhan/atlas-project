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
├── schemas/                    # Zod validation schemas
│   ├── gantt-project-plan.schema.ts
│   ├── schedule-delta.schema.ts
│   ├── task.schema.ts
│   ├── timeline-calendar.schema.ts
│   ├── resource.schema.ts      # Resource types and validation
│   ├── assignment.schema.ts    # Assignment and leveling types
│   ├── critical-path.schema.ts # Critical path analysis
│   └── analysis.schema.ts      # What-if and blocker chain analysis
├── types/                      # Type aliases
│   ├── AssignScheduleCommand.type.ts
│   ├── ChangeEndDateCommand.type.ts
│   ├── ChangeStartDateCommand.type.ts
│   ├── CreateTaskCommand.type.ts
│   ├── CreateTaskInPoolCommand.type.ts
│   ├── DeleteTaskCommand.type.ts
│   ├── LocalDate.type.ts
│   ├── MoveTaskCommand.type.ts
│   ├── PlanFromEndCommand.type.ts
│   ├── ResizeTaskCommand.type.ts
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
| `commands/assign-schedule-command.dto.ts` | `AssignScheduleCommandDto` | Assign schedule to pool task |
| `commands/plan-from-end-command.dto.ts` | `PlanFromEndCommandDto` | Plan backwards from end date |

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
    MoveTask = 'moveTask',
    ResizeTask = 'resizeTask',
    AssignSchedule = 'assignSchedule',
    PlanFromEnd = 'planFromEnd',
} as const

// TaskCommand.type.ts - Union type
export type TaskCommand =
    | CreateTaskInPoolCommand
    | ChangeStartDateCommand
    | ChangeEndDateCommand
    | UpdateTitleCommand
    | DeleteTaskCommand
    | MoveTaskCommand
    | ResizeTaskCommand
    | AssignScheduleCommand
    | PlanFromEndCommand

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

export type MoveTaskCommand = {
    type: typeof TaskCommandType.MoveTask
    taskId: string
    newStartDate: string
}

export type ResizeTaskCommand = {
    type: typeof TaskCommandType.ResizeTask
    taskId: string
    newEndDate: string
}

export type AssignScheduleCommand = {
    type: typeof TaskCommandType.AssignSchedule
    taskId: string
    start: string
    duration: number
}

export type PlanFromEndCommand = {
    type: typeof TaskCommandType.PlanFromEnd
    taskId: string
    newEndDate: string
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

## Resource Schema (`resource.schema.ts`)

### Purpose

Zod schemas for resource management validation.

### ResourceSchema

```typescript
export const ResourceSchema = z.object({
    id: z.string(),
    name: z.string(),
    type: z.enum(['PERSON', 'ROLE']),
    capacityHoursPerDay: z.number(),
    sortOrder: z.number().int().default(0),
})

export type Resource = z.infer<typeof ResourceSchema>
export type ResourceType = Resource['type']
```

**Fields:**
- `id` - Unique resource identifier
- `name` - Resource name (person name or role name)
- `type` - Either 'PERSON' or 'ROLE'
- `capacityHoursPerDay` - Default daily capacity in hours
- `sortOrder` - Display order in lists

---

### ResourceCalendarOverrideSchema

```typescript
export const ResourceCalendarOverrideSchema = z.object({
    date: z.string(),
    availableHours: z.number(),
})

export type ResourceCalendarOverride = z.infer<typeof ResourceCalendarOverrideSchema>
```

**Fields:**
- `date` - ISO date string
- `availableHours` - Available hours on this date (overrides capacity)

---

## Assignment Schema (`assignment.schema.ts`)

### Purpose

Zod schemas for task assignments, day overrides, and resource leveling.

### TaskAssignmentSchema

```typescript
export const TaskAssignmentSchema = z.object({
    id: z.string(),
    taskId: z.string(),
    resourceId: z.string(),
    hoursPerDay: z.number(),
    plannedEffortHours: z.number().nullable().optional(),
})

export type TaskAssignment = z.infer<typeof TaskAssignmentSchema>
```

**Fields:**
- `id` - Unique assignment identifier
- `taskId` - Reference to the task
- `resourceId` - Reference to the resource
- `hoursPerDay` - Daily allocation (0.5-24)
- `plannedEffortHours` - Optional total effort estimate

---

### AssignmentDayOverrideSchema

```typescript
export const AssignmentDayOverrideSchema = z.object({
    date: z.string(),
    hours: z.number(),
})

export type AssignmentDayOverride = z.infer<typeof AssignmentDayOverrideSchema>
```

**Fields:**
- `date` - ISO date string
- `hours` - Hours assigned on this specific day

---

### ResourceDayLoadSchema

```typescript
export const ResourceDayLoadSchema = z.object({
    date: z.string(),
    assignedHours: z.number(),
    capacityHours: z.number(),
    isOverloaded: z.boolean(),
})

export type ResourceDayLoad = z.infer<typeof ResourceDayLoadSchema>
```

**Fields:**
- `date` - ISO date string
- `assignedHours` - Total assigned hours on this day
- `capacityHours` - Available capacity on this day
- `isOverloaded` - True if assignedHours > capacityHours

---

### ResourceLoadResultSchema

```typescript
export const ResourceLoadResultSchema = z.object({
    resourceId: z.string(),
    resourceName: z.string(),
    days: z.array(ResourceDayLoadSchema),
    overloadedDaysCount: z.number().int(),
    allocatedHours: z.number(),
    effortDeficit: z.number().nullable().optional(),
})

export type ResourceLoadResult = z.infer<typeof ResourceLoadResultSchema>
```

---

### OverloadReportSchema

```typescript
export const OverloadReportSchema = z.object({
    resources: z.array(ResourceLoadResultSchema),
    totalOverloadedDays: z.number().int(),
    totalEffortDeficit: z.number(),
})

export type OverloadReport = z.infer<typeof OverloadReportSchema>
```

---

### LevelingResultSchema

```typescript
export const LevelingResultSchema = z.object({
    updatedSchedules: z.array(ScheduleUpdateSchema),
    resolvedOverloads: z.number().int(),
    remainingOverloads: z.number().int(),
})

export type LevelingResult = z.infer<typeof LevelingResultSchema>
```

---

## Analysis Schemas

### CriticalPathSchema (`critical-path.schema.ts`)

```typescript
export const CriticalPathSchema = z.object({
    criticalTasks: z.array(z.string()),
    totalSlack: z.number(),
})

export type CriticalPath = z.infer<typeof CriticalPathSchema>
```

---

### AnalysisSchemas (`analysis.schema.ts`)

```typescript
// BlockerChainDto
export const BlockerChainSchema = z.object({
    chain: z.array(z.string()),
    totalDelay: z.number(),
})

// AvailableTasksDto
export const AvailableTasksSchema = z.object({
    tasks: z.array(z.object({
        id: z.string(),
        title: z.string(),
        availableFrom: z.string(),
    })),
})

// WhatIfDto
export const WhatIfSchema = z.object({
    taskId: z.string(),
    originalStart: z.string(),
    newStart: z.string(),
    impact: z.array(z.object({
        taskId: z.string(),
        originalStart: z.string(),
        newStart: z.string(),
    })),
})
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
