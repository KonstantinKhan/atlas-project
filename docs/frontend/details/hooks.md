# Hooks - Detail

**Path:** `/frontend/atlas-project-web-app/src/hooks/`  
**Module:** [Frontend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

Custom React hooks that encapsulate API calls using TanStack React Query. They provide data fetching, caching, and mutation capabilities for the Gantt chart application.

## Files Overview

| File | Purpose |
|------|---------|
| `useProjectTasks.ts` | Task CRUD operation hooks |
| `useTimelineCalendar.ts` | Work calendar fetch hook |

---

## useProjectTasks.ts

### Purpose

Provides a collection of React Query hooks for project task operations including fetching, creating, updating, and managing task schedules.

### Exports

```typescript
// Query Hooks
export function useProjectPlan()
export function useProjectTasks()

// Mutation Hooks
export function useCreateProjectTask()
export function useUpdateProjectTask()
export function useChangeTaskStartDate()
export function useChangeTaskEndDate()
export function useCreateDependency()
```

### useProjectPlan

**Purpose:** Fetch the complete project plan with tasks and dependencies.

**Returns:**
```typescript
{
    data: GanttProjectPlan | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['projectPlan']`

**API Call:** `GET /project-plan`

---

### useProjectTasks

**Purpose:** Fetch the list of project tasks.

**Returns:**
```typescript
{
    data: Task[] | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['projectTasks']`

**API Call:** `GET /project-tasks`

---

### useCreateProjectTask

**Purpose:** Create a new project task in the pool (without schedule).

**Returns:**
```typescript
UseMutationResult<Task, Error, { title: string }>
```

**API Call:** `POST /project-tasks/create-in-pool`

**Usage Example:**
```typescript
const createMutation = useCreateProjectTask()

createMutation.mutate(
    { title: 'New Task' },
    {
        onSuccess: (data) => console.log('Created:', data),
        onError: (error) => console.error('Failed:', error)
    }
)
```

---

### useUpdateProjectTask

**Purpose:** Update an existing project task.

**Returns:**
```typescript
UseMutationResult<Task, Error, { id: string; updates: UpdateProjectTaskRequest }>
```

**API Call:** `PATCH /project-tasks/:id`

**Usage Example:**
```typescript
const updateMutation = useUpdateProjectTask()

updateMutation.mutate({
    id: 'task-123',
    updates: { title: 'Updated Title' }
})
```

---

### useChangeTaskStartDate

**Purpose:** Change a task's planned start date and recalculate schedule.

**Returns:**
```typescript
UseMutationResult<ScheduleDelta, Error, {
    planId: string
    taskId: string
    newPlannedStart: string
}>
```

**API Call:** `POST /change-start`

**Usage Example:**
```typescript
const changeStartMutation = useChangeTaskStartDate()

changeStartMutation.mutate({
    planId: 'plan-1',
    taskId: 'task-123',
    newPlannedStart: '2026-03-10'
})
```

---

### useChangeTaskEndDate

**Purpose:** Change a task's planned end date and recalculate schedule.

**Returns:**
```typescript
UseMutationResult<ScheduleDelta, Error, {
    planId: string
    taskId: string
    newPlannedEnd: string
}>
```

**API Call:** `POST /change-end`

---

### useCreateDependency

**Purpose:** Create a dependency between two tasks.

**Returns:**
```typescript
UseMutationResult<GanttProjectPlan, Error, {
    planId: string
    fromTaskId: string
    toTaskId: string
    type: string
}>
```

**API Call:** `POST /dependencies`

**Usage Example:**
```typescript
const createDepMutation = useCreateDependency()

createDepMutation.mutate({
    planId: 'plan-1',
    fromTaskId: 'task-1',
    toTaskId: 'task-2',
    type: 'FS' // Finish-to-Start
})
```

---

## useTimelineCalendar.ts

### Purpose

Fetch the work calendar configuration defining working days, weekends, and holidays.

### Signature

```typescript
export function useTimelineCalendar(): {
    calendar: TimelineCalendar | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

### Query Configuration

**Query Key:** `['timelineCalendar']`

**API Call:** `GET /work-calendar`

### Usage Example

```typescript
const { calendar, isLoading, error } = useTimelineCalendar()

if (isLoading) return <div>Loading...</div>
if (error) return <div>Error: {error.message}</div>
if (!calendar) return null

// Use calendar for date calculations
const isWorkingDay = calendar.isWorkingDay(date)
```

---

## Dependencies

**Imports:**
- `useQuery`, `useMutation` from `@tanstack/react-query`
- API functions from `@/services/projectTasksApi`, `@/services/timelineCalendarApi`
- Types from `@/types`

**Imported by:**
- `GanttChart.tsx`
- Other components needing task/calendar data

---

## Related Files

- [Services](./services.md)
- [Types](./types.md)
- [GanttChart Components](./components-gantt-chart.md)
