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
| `useResources.ts` | Resource CRUD and calendar override hooks |
| `useAssignments.ts` | Assignment, day override, and leveling hooks |

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
export function useResizeTaskFromStart()
export function useCreateDependency()
export function useChangeDependencyType()
export function useDeleteDependency()
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

### useResizeTaskFromStart

**Purpose:** Resize a task from its start date (left edge drag). Changes the start date while keeping the end date fixed, effectively changing the duration.

**Returns:**
```typescript
UseMutationResult<ScheduleDelta, Error, {
    taskId: string
    newPlannedStart: string
}>
```

**API Call:** `POST /resize-from-start`

**Features:**
- **Optimistic Update:** Immediately updates cache with new schedule
- **Rollback on Error:** Reverts to previous state if server request fails

**Usage Example:**
```typescript
const resizeFromStartMutation = useResizeTaskFromStart()

resizeFromStartMutation.mutate({
    taskId: 'task-123',
    newPlannedStart: '2026-03-15'
}, {
    onSuccess: (delta) => {
        console.log('Task resized from start:', delta)
    },
    onError: (error) => {
        console.error('Failed to resize:', error)
    }
})
```

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

### useChangeDependencyType

**Purpose:** Change the type of an existing dependency (e.g., from FS to SS).

**Returns:**
```typescript
UseMutationResult<GanttProjectPlan, Error, {
    fromTaskId: string
    toTaskId: string
    newType: string  // FS, SS, FF, SF
}>
```

**API Call:** `PATCH /dependencies`

**Features:**
- **Optimistic Update:** Immediately updates cache with new plan
- **Rollback on Error:** Reverts to previous state if server request fails

**Usage Example:**
```typescript
const changeTypeMutation = useChangeDependencyType()

changeTypeMutation.mutate({
    fromTaskId: 'task-1',
    toTaskId: 'task-2',
    newType: 'SS' // Change to Start-to-Start
}, {
    onSuccess: (newPlan) => {
        console.log('Dependency type changed:', newPlan)
    },
    onError: (error) => {
        console.error('Failed to change type:', error)
    }
})
```

---

### useDeleteDependency

**Purpose:** Remove a dependency between two tasks.

**Returns:**
```typescript
UseMutationResult<GanttProjectPlan, Error, {
    fromTaskId: string
    toTaskId: string
}>
```

**API Call:** `DELETE /dependencies?from={fromTaskId}&to={toTaskId}`

**Features:**
- **Optimistic Update:** Immediately updates cache with new plan
- **Recalculation:** Successor tasks may move earlier when constraint is removed
- **Rollback on Error:** Reverts to previous state if server request fails

**Usage Example:**
```typescript
const deleteDepMutation = useDeleteDependency()

deleteDepMutation.mutate({
    fromTaskId: 'task-1',
    toTaskId: 'task-2'
}, {
    onSuccess: (newPlan) => {
        console.log('Dependency removed:', newPlan)
    },
    onError: (error) => {
        console.error('Failed to delete dependency:', error)
    }
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

## useResources.ts

### Purpose

Provides React Query hooks for resource management including fetching, creating, updating, deleting resources, and managing calendar overrides.

### Exports

```typescript
// Query Hooks
export function useResources()
export function useCalendarOverrides(resourceId: string | null)

// Mutation Hooks
export function useCreateResource()
export function useUpdateResource()
export function useDeleteResource()
export function useSetCalendarOverride()
export function useDeleteCalendarOverride()
```

### useResources

**Purpose:** Fetch the list of all resources.

**Returns:**
```typescript
{
    data: Resource[] | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['resources']`

**API Call:** `GET /resources`

---

### useCalendarOverrides

**Purpose:** Fetch calendar overrides for a specific resource.

**Parameters:**
- `resourceId: string | null` - Resource ID (query disabled if null)

**Returns:**
```typescript
{
    data: ResourceCalendarOverride[] | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['calendarOverrides', resourceId]`

**API Call:** `GET /resources/:id/calendar-overrides`

---

### useCreateResource

**Purpose:** Create a new resource.

**Returns:**
```typescript
UseMutationResult<Resource, Error, {
    name: string
    type: string
    capacityHoursPerDay: number
}>
```

**API Call:** `POST /resources`

**Invalidates:** `['resources']`

---

### useUpdateResource

**Purpose:** Update an existing resource.

**Returns:**
```typescript
UseMutationResult<Resource, Error, {
    id: string
    updates: { name?: string; type?: string; capacityHoursPerDay?: number }
}>
```

**API Call:** `PATCH /resources/:id`

**Invalidates:** `['resources']`

---

### useDeleteResource

**Purpose:** Delete a resource.

**Returns:**
```typescript
UseMutationResult<void, Error, { id: string }>
```

**API Call:** `DELETE /resources/:id`

**Invalidates:** `['resources']`

---

### useSetCalendarOverride

**Purpose:** Set a calendar override for a resource on a specific date.

**Returns:**
```typescript
UseMutationResult<ResourceCalendarOverride, Error, {
    resourceId: string
    date: string
    availableHours: number
}>
```

**API Call:** `POST /resources/:id/calendar-overrides`

**Invalidates:** `['calendarOverrides', resourceId]`

---

### useDeleteCalendarOverride

**Purpose:** Delete a calendar override for a resource on a specific date.

**Returns:**
```typescript
UseMutationResult<void, Error, {
    resourceId: string
    date: string
}>
```

**API Call:** `DELETE /resources/:id/calendar-overrides/:date`

**Invalidates:** `['calendarOverrides', resourceId]`

---

## useAssignments.ts

### Purpose

Provides React Query hooks for task assignments, day overrides, and resource leveling operations.

### Exports

```typescript
// Query Hooks
export function useAssignments()
export function useDayOverrides(assignmentId: string | null)
export function useResourceLoad(from: string | null, to: string | null)

// Mutation Hooks
export function useCreateAssignment()
export function useUpdateAssignment()
export function useDeleteAssignment()
export function useSetDayOverride()
export function useDeleteDayOverride()
export function useLevelingPreview()
export function useApplyLeveling()
```

### useAssignments

**Purpose:** Fetch all task assignments.

**Returns:**
```typescript
{
    data: TaskAssignment[] | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['assignments']`

**API Call:** `GET /assignments`

---

### useDayOverrides

**Purpose:** Fetch day overrides for a specific assignment.

**Parameters:**
- `assignmentId: string | null` - Assignment ID (query disabled if null)

**Returns:**
```typescript
{
    data: AssignmentDayOverride[] | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['dayOverrides', assignmentId]`

**API Call:** `GET /assignments/:id/day-overrides`

---

### useResourceLoad

**Purpose:** Fetch resource load report for a date range.

**Parameters:**
- `from: string | null` - Start date (ISO string)
- `to: string | null` - End date (ISO string)

**Returns:**
```typescript
{
    data: OverloadReport | undefined
    isLoading: boolean
    error: Error | null
    refetch: () => void
}
```

**Query Key:** `['resourceLoad', from, to]`

**API Call:** `GET /resource-load?from=:from&to=:to`

---

### useCreateAssignment

**Purpose:** Create a new task assignment.

**Returns:**
```typescript
UseMutationResult<TaskAssignment, Error, {
    taskId: string
    resourceId: string
    hoursPerDay?: number
}>
```

**API Call:** `POST /assignments`

**Invalidates:** `['assignments']`, `['resourceLoad']`

---

### useUpdateAssignment

**Purpose:** Update an existing assignment.

**Returns:**
```typescript
UseMutationResult<TaskAssignment, Error, {
    id: string
    hoursPerDay?: number
    plannedEffortHours?: number | null
}>
```

**API Call:** `PATCH /assignments/:id`

**Invalidates:** `['assignments']`, `['resourceLoad']`

---

### useDeleteAssignment

**Purpose:** Delete an assignment.

**Returns:**
```typescript
UseMutationResult<void, Error, { id: string }>
```

**API Call:** `DELETE /assignments/:id`

**Invalidates:** `['assignments']`, `['resourceLoad']`

---

### useSetDayOverride

**Purpose:** Set a day override for an assignment.

**Returns:**
```typescript
UseMutationResult<AssignmentDayOverride, Error, {
    assignmentId: string
    date: string
    hours: number
}>
```

**API Call:** `POST /assignments/:id/day-overrides`

**Invalidates:** `['dayOverrides']`, `['resourceLoad']`

---

### useDeleteDayOverride

**Purpose:** Delete a day override for an assignment.

**Returns:**
```typescript
UseMutationResult<void, Error, {
    assignmentId: string
    date: string
}>
```

**API Call:** `DELETE /assignments/:id/day-overrides/:date`

**Invalidates:** `['dayOverrides']`, `['resourceLoad']`

---

### useLevelingPreview

**Purpose:** Preview resource leveling result without applying changes.

**Returns:**
```typescript
UseMutationResult<LevelingResult, Error, void>
```

**API Call:** `POST /leveling/preview`

**Usage Example:**
```typescript
const levelingMutation = useLevelingPreview()

levelingMutation.mutate(undefined, {
    onSuccess: (result) => {
        console.log('Resolved overloads:', result.resolvedOverloads)
        console.log('Remaining overloads:', result.remainingOverloads)
        console.log('Schedule changes:', result.updatedSchedules)
    }
})
```

---

### useApplyLeveling

**Purpose:** Apply resource leveling changes to the project schedule.

**Returns:**
```typescript
UseMutationResult<LevelingResult, Error, void>
```

**API Call:** `POST /leveling/apply`

**Invalidates:** `['projectPlan']`, `['resourceLoad']`, `['criticalPath']`

**Usage Example:**
```typescript
const applyMutation = useApplyLeveling()

applyMutation.mutate(undefined, {
    onSuccess: (result) => {
        console.log('Leveling applied successfully')
        console.log('Tasks rescheduled:', result.updatedSchedules.length)
    }
})
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
