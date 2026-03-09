# Frontend Hooks

**Path:** `/frontend/atlas-project-web-app/src/hooks`  
**Last Updated:** 2026-03-09

## Overview

Custom React hooks for data fetching, state management, and business logic. All hooks use TanStack Query (React Query) for server state management.

## Available Hooks

### useProjectTasks

**File:** `useProjectTasks.ts`

Legacy hook for fetching project tasks. Prefer using individual hooks below.

```typescript
const { data, isLoading, error } = useProjectTasks()
```

### useProjectPlan

**File:** `useProjectTasks.ts`

Fetches the complete project plan including tasks and dependencies.

```typescript
const {
  data: planData,
  isLoading: planLoading,
  error: planError,
  refetch: refetchPlan,
} = useProjectPlan()

// Returns: GanttProjectPlan
// {
//   projectId: string
//   tasks: GanttTask[]
//   dependencies: GanttDependencyDto[]
// }
```

### useCriticalPath

**File:** `useProjectTasks.ts`

Fetches critical path analysis for the project.

```typescript
const { data: criticalPathData } = useCriticalPath()

// Returns: CriticalPath
// {
//   criticalTaskIds: string[]
//   tasks: { taskId: string; slack: number }[]
// }
```

### useTimelineCalendar

**File:** `useTimelineCalendar.ts`

Fetches and updates the timeline calendar configuration.

```typescript
const {
  calendar,
  isLoading,
  error,
  refetch,
  updateCalendar,
  isUpdating,
} = useTimelineCalendar()

// Usage:
await updateCalendar(newCalendarData)
```

### Mutation Hooks

#### useCreateProjectTask

Creates a new task in the pool.

```typescript
const createTaskMutation = useCreateProjectTask()

createTaskMutation.mutate(
  { title: 'New Task' },
  {
    onSuccess: () => console.log('Task created'),
    onError: () => console.error('Failed to create task'),
  }
)
```

#### useUpdateProjectTask

Updates task fields (title, description, status).

```typescript
const updateTitleMutation = useUpdateProjectTask()

updateTitleMutation.mutate({
  id: 'task-123',
  updates: { title: 'Updated Title' }
})
```

#### useChangeTaskStartDate

Changes task start date with automatic schedule recalculation.

```typescript
const changeStartMutation = useChangeTaskStartDate()

changeStartMutation.mutate({
  planId: 'plan-1',
  taskId: 'task-123',
  newPlannedStart: '2026-03-15'
})
```

#### useChangeTaskEndDate

Changes task end date with automatic schedule recalculation.

```typescript
const changeEndMutation = useChangeTaskEndDate()

changeEndMutation.mutate({
  planId: 'plan-1',
  taskId: 'task-123',
  newPlannedEnd: '2026-03-20'
})
```

#### useResizeTaskFromStart

Resizes task from the start (moves start date, keeps end).

```typescript
const resizeFromStartMutation = useResizeTaskFromStart()

resizeFromStartMutation.mutate({
  planId: 'plan-1',
  taskId: 'task-123',
  newPlannedStart: '2026-03-10'
})
```

#### useAssignTaskSchedule

Assigns a schedule to an unscheduled task.

```typescript
const assignScheduleMutation = useAssignTaskSchedule()

assignScheduleMutation.mutate({
  taskId: 'task-123',
  start: '2026-03-15',
  duration: 5
})
```

#### usePlanFromEnd

Plans task backward from end date.

```typescript
const planFromEndMutation = usePlanFromEnd()

planFromEndMutation.mutate({
  taskId: 'task-123',
  newPlannedEnd: '2026-03-25'
})
```

#### useDeleteProjectTask

Deletes a task and its dependencies.

```typescript
const deleteTaskMutation = useDeleteProjectTask()

deleteTaskMutation.mutate(
  { id: 'task-123' },
  {
    onSuccess: () => console.log('Task deleted'),
  }
)
```

#### useCreateDependency

Creates a dependency between tasks.

```typescript
const createDependencyMutation = useCreateDependency()

createDependencyMutation.mutate({
  planId: 'plan-1',
  fromTaskId: 'task-1',
  toTaskId: 'task-2',
  type: 'FS' // Finish-Start
})
```

#### useDeleteDependency

Removes a dependency.

```typescript
const deleteDependencyMutation = useDeleteDependency()

deleteDependencyMutation.mutate({
  fromTaskId: 'task-1',
  toTaskId: 'task-2'
})
```

#### useChangeDependencyType

Changes the type of an existing dependency.

```typescript
const changeDependencyTypeMutation = useChangeDependencyType()

changeDependencyTypeMutation.mutate({
  fromTaskId: 'task-1',
  toTaskId: 'task-2',
  newType: 'SS' // Start-Start
})
```

## Query Keys

```typescript
const QUERY_KEY = ['projectTasks']
const CRITICAL_PATH_KEY = ['criticalPath']
const PROJECT_PLAN_KEY = ['projectPlan']
const TIMELINE_CALENDAR_KEY = ['timelineCalendar']
```

## Optimistic Updates

All mutation hooks implement optimistic updates with rollback on error:

```typescript
// Example from useChangeTaskStartDate
onSuccess: (delta: ScheduleDelta) => {
  queryClient.invalidateQueries({ queryKey: CRITICAL_PATH_KEY })
  queryClient.setQueryData<GanttProjectPlan>(['projectPlan'], (old) => {
    if (!old) return old
    return {
      ...old,
      tasks: old.tasks.map((t) => {
        const update = delta.updatedSchedules.find((u) => u.taskId === t.id)
        return update ? { ...t, start: update.start, end: update.end } : t
      }),
    }
  })
}
```

## Patterns

### Query Invalidation

After mutations, related queries are invalidated:

```typescript
onSuccess: () => {
  queryClient.invalidateQueries({ queryKey: QUERY_KEY })
  queryClient.invalidateQueries({ queryKey: ['projectPlan'] })
  queryClient.invalidateQueries({ queryKey: CRITICAL_PATH_KEY })
}
```

### Error Handling

Errors are handled in the component layer via toast notifications:

```typescript
createTaskMutation.mutate(
  { title: 'New Task' },
  {
    onError: () => setToastMessage('Не удалось создать задачу'),
  }
)
```

## Related Files

- [Services](../services/README.md) - API service layer
- [Store](../store/README.md) - Zustand store
- [Components](../components/README.md) - UI components
