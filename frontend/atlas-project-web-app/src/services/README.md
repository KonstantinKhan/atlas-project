# Frontend Services

**Path:** `/frontend/atlas-project-web-app/src/services`  
**Last Updated:** 2026-03-09

## Overview

Service layer for API communication. All services use the native Fetch API with Zod schema validation for type-safe responses.

## Services

### projectTasksApi.ts

Main API service for project task operations.

**Base URL:**
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
```

#### Functions

##### getProjectTasks()

Fetches all project tasks.

```typescript
export async function getProjectTasks(): Promise<Task[]>
```

##### updateProjectTask(id, updates)

Updates a task's fields.

```typescript
export async function updateProjectTask(
  id: string,
  updates: object,
): Promise<Task>
```

##### getProjectPlan()

Fetches the complete project plan with tasks and dependencies.

```typescript
export async function getProjectPlan(): Promise<GanttProjectPlan>
```

##### changeTaskStartDate(planId, taskId, newPlannedStart)

Changes task start date.

```typescript
export async function changeTaskStartDate(
  planId: string,
  taskId: string,
  newPlannedStart: string,
): Promise<ScheduleDelta>
```

##### resizeTaskFromStart(planId, taskId, newPlannedStart)

Resizes task from start.

```typescript
export async function resizeTaskFromStart(
  planId: string,
  taskId: string,
  newPlannedStart: string,
): Promise<ScheduleDelta>
```

##### changeTaskEndDate(planId, taskId, newPlannedEnd)

Changes task end date.

```typescript
export async function changeTaskEndDate(
  planId: string,
  taskId: string,
  newPlannedEnd: string,
): Promise<ScheduleDelta>
```

##### createProjectTask(title)

Creates a new task.

```typescript
export async function createProjectTask(title: string): Promise<Task>
```

##### deleteProjectTask(id)

Deletes a task.

```typescript
export async function deleteProjectTask(id: string): Promise<void>
```

##### assignTaskSchedule(taskId, start, duration)

Assigns schedule to a task.

```typescript
export async function assignTaskSchedule(
  taskId: string,
  start: string,
  duration: number,
): Promise<GanttProjectPlan>
```

##### planTaskFromEnd(taskId, newPlannedEnd)

Plans task backward from end date.

```typescript
export async function planTaskFromEnd(
  taskId: string,
  newPlannedEnd: string,
): Promise<GanttProjectPlan>
```

##### deleteDependency(fromTaskId, toTaskId)

Removes a dependency.

```typescript
export async function deleteDependency(
  fromTaskId: string,
  toTaskId: string,
): Promise<GanttProjectPlan>
```

##### changeDependencyType(fromTaskId, toTaskId, newType)

Changes dependency type.

```typescript
export async function changeDependencyType(
  fromTaskId: string,
  toTaskId: string,
  newType: string,
): Promise<GanttProjectPlan>
```

##### getCriticalPath()

Fetches critical path analysis.

```typescript
export async function getCriticalPath(): Promise<CriticalPath>
```

##### createDependency(planId, fromTaskId, toTaskId, type)

Creates a new dependency.

```typescript
export async function createDependency(
  planId: string,
  fromTaskId: string,
  toTaskId: string,
  type: string = 'FS',
): Promise<GanttProjectPlan>
```

### timelineCalendarApi.ts

API service for timeline calendar operations.

#### Functions

##### getTimelineCalendar()

Fetches the timeline calendar configuration.

```typescript
export async function getTimelineCalendar(): Promise<TimelineCalendar>
```

##### updateTimelineCalendar(calendar)

Updates the timeline calendar.

```typescript
export async function updateTimelineCalendar(
  calendar: TimelineCalendar
): Promise<TimelineCalendar>
```

## Response Schemas

All responses are validated with Zod schemas:

```typescript
// Task schema
export const TaskSchema = z.object({
  id: z.string(),
  title: z.string(),
  description: z.string(),
  start: z.string().nullable(),
  end: z.string().nullable(),
  status: ProjectTaskStatusSchema,
})

// GanttProjectPlan schema
export const GanttProjectPlanSchema = z.object({
  projectId: z.string(),
  tasks: GanttTaskListSchema,
  dependencies: z.array(GanttDependencyDtoSchema),
})

// ScheduleDelta schema
export const ScheduleDeltaSchema = z.object({
  updatedSchedules: z.array(z.object({
    taskId: z.string(),
    start: z.string(),
    end: z.string(),
  })),
})

// CriticalPath schema
export const CriticalPathSchema = z.object({
  criticalTaskIds: z.array(z.string()),
  tasks: z.array(z.object({
    taskId: z.string(),
    slack: z.number(),
  })),
})
```

## Error Handling

All functions throw errors on non-OK responses:

```typescript
if (!response.ok) throw new Error('Failed to fetch project tasks')
if (!res.ok) throw new Error('Failed to update project task')
```

Errors are caught and handled in the React Query mutation hooks.

## Usage Example

```typescript
import { getProjectPlan, createProjectTask } from '@/services/projectTasksApi'

// Fetch project plan
const plan = await getProjectPlan()
console.log(plan.tasks)

// Create task
const newTask = await createProjectTask('New Task')
console.log(newTask.id)
```

## Related Files

- [Hooks](../hooks/README.md) - React Query hooks
- [Types](../types/README.md) - TypeScript types and schemas
