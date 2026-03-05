# Hooks - Detail

**Path:** `/frontend/atlas-project-web-app/src/hooks/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-04

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
export function useDeleteProjectTask()
export function useChangeTaskStartDate()
export function useChangeTaskEndDate()
export function useCreateDependency()
export function useAssignTaskSchedule()
export function usePlanFromEnd()
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

### useDeleteProjectTask

**Purpose:** Delete a project task and its associated schedules and dependencies.

**Returns:**
```typescript
UseMutationResult<void, Error, { id: string }>
```

**API Call:** `DELETE /project-tasks/:id`

**Features:**
- **Optimistic Update:** Immediately removes task from cache before server response
- **Cascade Cleanup:** Automatically removes task from dependencies list
- **Rollback on Error:** Reverts cache if server request fails

**Usage Example:**
```typescript
const deleteMutation = useDeleteProjectTask()

deleteMutation.mutate(
    { id: 'task-123' },
    {
        onSuccess: () => {
            // Task already removed from cache via optimistic update
            console.log('Task deleted successfully')
        },
        onError: (error) => {
            // Cache will be reverted automatically by React Query
            console.error('Failed to delete:', error)
        }
    }
)
```

**Optimistic Update Implementation:**
```typescript
export function useDeleteProjectTask() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: ({ id }: { id: string }) => deleteProjectTask(id),
        onSuccess: (_, { id }) => {
            queryClient.setQueryData<GanttProjectPlan>(['projectPlan'], (old) => {
                if (!old) return old
                return {
                    ...old,
                    tasks: old.tasks.filter((t) => t.id !== id),
                    dependencies: old.dependencies.filter(
                        (d) => d.fromTaskId !== id && d.toTaskId !== id
                    ),
                }
            })
        },
    })
}
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

### useAssignTaskSchedule

**Purpose:** Assign a schedule to an existing task (typically for pool tasks being dragged to the timeline).

**Returns:**
```typescript
UseMutationResult<GanttProjectPlan, Error, {
    taskId: string
    start: string
    duration: number
}>
```

**API Call:** `POST /project-tasks/{taskId}/schedule`

**Features:**
- **Optimistic Update:** Immediately updates cache with new schedule before server response
- **Rollback on Error:** Reverts to previous state if server request fails

**Usage Example:**
```typescript
const assignMutation = useAssignTaskSchedule()

assignMutation.mutate({
    taskId: 'task-123',
    start: '2026-03-15',
    duration: 5  // 5 days
}, {
    onSuccess: (newPlan) => {
        console.log('Task scheduled:', newPlan)
    },
    onError: (error) => {
        console.error('Failed to schedule:', error)
        // Cache automatically reverted
    }
})
```

**Implementation:**
```typescript
export function useAssignTaskSchedule() {
    const queryClient = useQueryClient()
    return useMutation<GanttProjectPlan, Error, { taskId: string; start: string; duration: number }>({
        mutationFn: ({ taskId, start, duration }) => assignTaskSchedule(taskId, start, duration),
        onSuccess: (newPlan: GanttProjectPlan) => {
            queryClient.setQueryData<GanttProjectPlan>(['projectPlan'], () => newPlan)
        },
    })
}
```

---

### usePlanFromEnd

**Purpose:** Plan a task backwards from its end date (useful for deadline-driven scheduling).

**Returns:**
```typescript
UseMutationResult<GanttProjectPlan, Error, {
    taskId: string
    newPlannedEnd: string
}>
```

**API Call:** `POST /plan-from-end`

**Features:**
- **Backwards Planning:** Calculates start date by working backwards from end date
- **Working Days Respect:** Uses calendar to skip weekends and holidays
- **Optimistic Update:** Immediately updates cache with new plan

**Usage Example:**
```typescript
const planFromEndMutation = usePlanFromEnd()

planFromEndMutation.mutate({
    taskId: 'task-123',
    newPlannedEnd: '2026-04-01'  // Deadline
}, {
    onSuccess: (newPlan) => {
        console.log('Task planned from end:', newPlan)
    },
    onError: (error) => {
        console.error('Failed to plan from end:', error)
    }
})
```

**Implementation:**
```typescript
export function usePlanFromEnd() {
    const queryClient = useQueryClient()
    return useMutation<GanttProjectPlan, Error, { taskId: string; newPlannedEnd: string }>({
        mutationFn: ({ taskId, newPlannedEnd }) => planTaskFromEnd(taskId, newPlannedEnd),
        onSuccess: (newPlan: GanttProjectPlan) => {
            queryClient.setQueryData<GanttProjectPlan>(['projectPlan'], () => newPlan)
        },
    })
}
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
