# Services - Detail

**Path:** `/frontend/atlas-project-web-app/src/services/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-04

## Purpose

Service layer providing API integration functions for communicating with the backend. All functions use the native Fetch API and Zod for response validation.

## Files Overview

| File | Purpose |
|------|---------|
| `projectTasksApi.ts` | Task CRUD and schedule operations |
| `timelineCalendarApi.ts` | Work calendar operations |

---

## projectTasksApi.ts

### Purpose

API service functions for project task operations including fetching, creating, updating tasks, and managing schedules/dependencies.

### Configuration

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
```

### Functions

#### getProjectTasks()

**Purpose:** Fetch all project tasks.

**Signature:**
```typescript
export async function getProjectTasks(): Promise<Task[]>
```

**API:** `GET /project-tasks`

**Returns:** Array of `Task` objects validated by `TaskListSchema`

**Throws:** Error if response is not OK

---

#### updateProjectTask(id, updates)

**Purpose:** Update an existing task.

**Signature:**
```typescript
export async function updateProjectTask(
    id: string,
    updates: object
): Promise<Task>
```

**Parameters:**
- `id` - Task ID to update
- `updates` - Partial update object

**API:** `PATCH /project-tasks/:id`

**Returns:** Updated `Task` object

---

#### getProjectPlan()

**Purpose:** Fetch the complete project plan with tasks and dependencies.

**Signature:**
```typescript
export async function getProjectPlan(): Promise<GanttProjectPlan>
```

**API:** `GET /project-plan`

**Returns:** `GanttProjectPlan` with tasks and dependencies

---

#### changeTaskStartDate(planId, taskId, newPlannedStart)

**Purpose:** Change a task's start date and get the schedule delta.

**Signature:**
```typescript
export async function changeTaskStartDate(
    planId: string,
    taskId: string,
    newPlannedStart: string
): Promise<ScheduleDelta>
```

**Parameters:**
- `planId` - Project plan ID
- `taskId` - Task ID to update
- `newPlannedStart` - New start date (ISO string)

**API:** `POST /change-start`

**Returns:** `ScheduleDelta` with updated schedules

---

#### changeTaskEndDate(planId, taskId, newPlannedEnd)

**Purpose:** Change a task's end date and get the schedule delta.

**Signature:**
```typescript
export async function changeTaskEndDate(
    planId: string,
    taskId: string,
    newPlannedEnd: string
): Promise<ScheduleDelta>
```

**API:** `POST /change-end`

---

#### createProjectTask(title)

**Purpose:** Create a new task in the pool (without schedule).

**Signature:**
```typescript
export async function createProjectTask(title: string): Promise<Task>
```

**API:** `POST /project-tasks/create-in-pool`

**Returns:** Created `Task` object

---

#### deleteProjectTask(id)

**Purpose:** Delete an existing project task.

**Signature:**
```typescript
export async function deleteProjectTask(id: string): Promise<void>
```

**Parameters:**
- `id` - Task ID to delete

**API:** `DELETE /project-tasks/:id`

**Returns:** `void` (204 No Content on success)

**Throws:** Error if response is not OK

**Usage Example:**
```typescript
await deleteProjectTask('task-123')
// Task deleted, no content returned
```

---

#### createDependency(planId, fromTaskId, toTaskId, type)

**Purpose:** Create a dependency between two tasks.

**Signature:**
```typescript
export async function createDependency(
    planId: string,
    fromTaskId: string,
    toTaskId: string,
    type?: string
): Promise<GanttProjectPlan>
```

**Parameters:**
- `planId` - Project plan ID
- `fromTaskId` - Predecessor task ID
- `toTaskId` - Successor task ID
- `type` - Dependency type (default: 'FS' - Finish-to-Start)

**API:** `POST /dependencies`

**Returns:** Updated `GanttProjectPlan` with new dependency

---

### Dependencies

**Imports:**
- Zod schemas from `@/types`
- Types from `@/types`

**Imported by:**
- `hooks/useProjectTasks.ts`

---

## timelineCalendarApi.ts

### Purpose

API service functions for work calendar operations.

### Functions

#### getTimelineCalendar()

**Purpose:** Fetch the work calendar configuration.

**Signature:**
```typescript
export async function getTimelineCalendar(): Promise<TimelineCalendar>
```

**API:** `GET /work-calendar`

**Returns:** `TimelineCalendar` object validated by `TimelineCalendarSchema`

**Throws:** Error if response is not OK

### Dependencies

**Imports:**
- `TimelineCalendarSchema`, `TimelineCalendar` from `@/types`

**Imported by:**
- `hooks/useTimelineCalendar.ts`

---

## Error Handling

All service functions follow a consistent error handling pattern:

```typescript
if (!response.ok) throw new Error('Descriptive error message')
```

Errors are caught and handled by React Query mutations/queries in the hooks layer.

---

## Related Files

- [Hooks](./hooks.md)
- [Types](./types.md)
- [Backend API](../../backend/details/ktor-app.md)
