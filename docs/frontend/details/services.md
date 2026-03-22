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
| `resourcesApi.ts` | Resource CRUD and calendar override operations |
| `assignmentsApi.ts` | Assignment, day override, and leveling operations |

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

#### changeDependencyType(fromTaskId, toTaskId, newType)

**Purpose:** Change the type of an existing dependency (e.g., from FS to SS).

**Signature:**
```typescript
export async function changeDependencyType(
    fromTaskId: string,
    toTaskId: string,
    newType: string,  // FS, SS, FF, SF
): Promise<GanttProjectPlan>
```

**Parameters:**
- `fromTaskId` - Predecessor task ID
- `toTaskId` - Successor task ID
- `newType` - New dependency type (FS, SS, FF, SF)

**API:** `PATCH /dependencies`

**Returns:** Updated `GanttProjectPlan` with recalculated schedules

**Throws:** Error if response is not OK

**Usage Example:**
```typescript
await changeDependencyType('task-1', 'task-2', 'SS')
// Dependency changed to Start-to-Start, schedules recalculated
```

---

#### deleteDependency(fromTaskId, toTaskId)

**Purpose:** Remove a dependency between two tasks.

**Signature:**
```typescript
export async function deleteDependency(
    fromTaskId: string,
    toTaskId: string,
): Promise<GanttProjectPlan>
```

**Parameters:**
- `fromTaskId` - Predecessor task ID
- `toTaskId` - Successor task ID

**API:** `DELETE /dependencies?from={fromTaskId}&to={toTaskId}`

**Returns:** Updated `GanttProjectPlan` with dependency removed

**Throws:** Error if response is not OK

**Usage Example:**
```typescript
await deleteDependency('task-1', 'task-2')
// Dependency removed, successor may move earlier
```

---

#### resizeTaskFromStart(taskId, newPlannedStart)

**Purpose:** Resize a task from its start date (left edge drag). Changes the start date while keeping the end date fixed.

**Signature:**
```typescript
export async function resizeTaskFromStart(
    taskId: string,
    newPlannedStart: string,
): Promise<ScheduleDelta>
```

**Parameters:**
- `taskId` - Task ID to resize
- `newPlannedStart` - New start date (ISO string)

**API:** `POST /resize-from-start`

**Returns:** `ScheduleDelta` with updated schedules

**Throws:** Error if response is not OK

**Usage Example:**
```typescript
await resizeTaskFromStart('task-123', '2026-03-15')
// Task start changed, duration adjusted to keep end date fixed
```

---

#### assignTaskSchedule(taskId, start, duration)

**Purpose:** Assign a schedule to an existing task (typically for pool tasks being dragged to timeline).

**Signature:**
```typescript
export async function assignTaskSchedule(
    taskId: string,
    start: string,
    duration: number,
): Promise<GanttProjectPlan>
```

**Parameters:**
- `taskId` - Task ID to assign schedule to
- `start` - Start date (ISO string)
- `duration` - Duration in days

**API:** `POST /project-tasks/{taskId}/schedule`

**Returns:** Updated `GanttProjectPlan` with new task schedule

**Throws:** Error if response is not OK

**Usage Example:**
```typescript
await assignTaskSchedule('task-123', '2026-03-15', 5)
// Task scheduled from 2026-03-15 for 5 days
```

---

#### planTaskFromEnd(taskId, newPlannedEnd)

**Purpose:** Plan a task backwards from its end date (useful for deadline-driven scheduling).

**Signature:**
```typescript
export async function planTaskFromEnd(
    taskId: string,
    newPlannedEnd: string,
): Promise<GanttProjectPlan>
```

**Parameters:**
- `taskId` - Task ID to plan
- `newPlannedEnd` - Target end date (ISO string)

**API:** `POST /plan-from-end`

**Returns:** Updated `GanttProjectPlan` with recalculated start date

**Throws:** Error if response is not OK

**Usage Example:**
```typescript
await planTaskFromEnd('task-123', '2026-04-01')
// Task start date calculated backwards from 2026-04-01 deadline
```

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

## resourcesApi.ts

### Purpose

API service functions for resource management including CRUD operations and calendar overrides.

### Configuration

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
```

### Functions

#### getResources()

**Purpose:** Fetch all resources.

**Signature:**
```typescript
export async function getResources(): Promise<Resource[]>
```

**API:** `GET /resources`

**Returns:** Array of `Resource` objects validated by `ResourceListSchema`

---

#### createResource(name, type, capacityHoursPerDay)

**Purpose:** Create a new resource.

**Signature:**
```typescript
export async function createResource(
    name: string,
    type: string,
    capacityHoursPerDay: number
): Promise<Resource>
```

**API:** `POST /resources`

**Returns:** Created `Resource` object

---

#### updateResource(id, updates)

**Purpose:** Update an existing resource.

**Signature:**
```typescript
export async function updateResource(
    id: string,
    updates: { name?: string; type?: string; capacityHoursPerDay?: number }
): Promise<Resource>
```

**API:** `PATCH /resources/:id`

**Returns:** Updated `Resource` object

---

#### deleteResource(id)

**Purpose:** Delete a resource.

**Signature:**
```typescript
export async function deleteResource(id: string): Promise<void>
```

**API:** `DELETE /resources/:id`

**Returns:** `void` (204 No Content)

---

#### getCalendarOverrides(resourceId)

**Purpose:** Fetch calendar overrides for a resource.

**Signature:**
```typescript
export async function getCalendarOverrides(resourceId: string): Promise<ResourceCalendarOverride[]>
```

**API:** `GET /resources/:id/calendar-overrides`

**Returns:** Array of `ResourceCalendarOverride` objects

---

#### setCalendarOverride(resourceId, date, availableHours)

**Purpose:** Set a calendar override for a resource on a specific date.

**Signature:**
```typescript
export async function setCalendarOverride(
    resourceId: string,
    date: string,
    availableHours: number
): Promise<ResourceCalendarOverride>
```

**API:** `POST /resources/:id/calendar-overrides`

**Returns:** Created `ResourceCalendarOverride` object

---

#### deleteCalendarOverride(resourceId, date)

**Purpose:** Delete a calendar override.

**Signature:**
```typescript
export async function deleteCalendarOverride(
    resourceId: string,
    date: string
): Promise<void>
```

**API:** `DELETE /resources/:id/calendar-overrides/:date`

**Returns:** `void` (204 No Content)

### Dependencies

**Imports:**
- Schemas from `@/types/schemas/resource.schema`

**Imported by:**
- `hooks/useResources.ts`

---

## assignmentsApi.ts

### Purpose

API service functions for task assignments, day overrides, and resource leveling operations.

### Configuration

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
```

### Functions

#### getAssignments()

**Purpose:** Fetch all task assignments.

**Signature:**
```typescript
export async function getAssignments(): Promise<TaskAssignment[]>
```

**API:** `GET /assignments`

**Returns:** Array of `TaskAssignment` objects validated by `TaskAssignmentListSchema`

---

#### createAssignment(taskId, resourceId, hoursPerDay, plannedEffortHours)

**Purpose:** Create a new task assignment.

**Signature:**
```typescript
export async function createAssignment(
    taskId: string,
    resourceId: string,
    hoursPerDay?: number,
    plannedEffortHours?: number | null
): Promise<TaskAssignment>
```

**API:** `POST /assignments`

**Returns:** Created `TaskAssignment` object

---

#### updateAssignment(id, updates)

**Purpose:** Update an existing assignment.

**Signature:**
```typescript
export async function updateAssignment(
    id: string,
    updates: { hoursPerDay?: number; plannedEffortHours?: number | null }
): Promise<TaskAssignment>
```

**API:** `PATCH /assignments/:id`

**Returns:** Updated `TaskAssignment` object

---

#### deleteAssignment(id)

**Purpose:** Delete an assignment.

**Signature:**
```typescript
export async function deleteAssignment(id: string): Promise<void>
```

**API:** `DELETE /assignments/:id`

**Returns:** `void` (204 No Content)

---

#### getDayOverrides(assignmentId)

**Purpose:** Fetch day overrides for an assignment.

**Signature:**
```typescript
export async function getDayOverrides(assignmentId: string): Promise<AssignmentDayOverride[]>
```

**API:** `GET /assignments/:id/day-overrides`

**Returns:** Array of `AssignmentDayOverride` objects

---

#### setDayOverride(assignmentId, date, hours)

**Purpose:** Set a day override for an assignment.

**Signature:**
```typescript
export async function setDayOverride(
    assignmentId: string,
    date: string,
    hours: number
): Promise<AssignmentDayOverride>
```

**API:** `POST /assignments/:id/day-overrides`

**Returns:** Created `AssignmentDayOverride` object

---

#### deleteDayOverride(assignmentId, date)

**Purpose:** Delete a day override.

**Signature:**
```typescript
export async function deleteDayOverride(
    assignmentId: string,
    date: string
): Promise<void>
```

**API:** `DELETE /assignments/:id/day-overrides/:date`

**Returns:** `void` (204 No Content)

---

#### getResourceLoad(from, to)

**Purpose:** Fetch resource load report for a date range.

**Signature:**
```typescript
export async function getResourceLoad(from: string, to: string): Promise<OverloadReport>
```

**API:** `GET /resource-load?from=:from&to=:to`

**Returns:** `OverloadReport` object with per-resource load data

---

#### previewLeveling()

**Purpose:** Preview resource leveling result.

**Signature:**
```typescript
export async function previewLeveling(): Promise<LevelingResult>
```

**API:** `POST /leveling/preview`

**Returns:** `LevelingResult` with updated schedules and overload counts

---

#### applyLeveling()

**Purpose:** Apply resource leveling changes.

**Signature:**
```typescript
export async function applyLeveling(): Promise<LevelingResult>
```

**API:** `POST /leveling/apply`

**Returns:** `LevelingResult` with applied changes

### Dependencies

**Imports:**
- Schemas from `@/types/schemas/assignment.schema`

**Imported by:**
- `hooks/useAssignments.ts`

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
