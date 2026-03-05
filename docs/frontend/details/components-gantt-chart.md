# GanttChart Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/GanttChart/`  
**Module:** [Frontend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

The GanttChart module provides the main visualization component for project task management. It renders an interactive Gantt chart with a task list, calendar grid, and dependency visualization.

## Files Overview

| File | Purpose |
|------|---------|
| `GanttChart.tsx` | Main container component with state management |
| `GanttTaskList.tsx` | Left panel showing task titles |
| `GanttCalendarHeader.tsx` | Calendar header with month/day labels |
| `GanttCalendarGrid.tsx` | Grid rendering tasks and dependencies |
| `GanttTaskRow.tsx` | Individual task row in the grid |
| `GanttTaskLayer.tsx` | Layer for rendering task bars |
| `GanttBar.tsx` | Individual task bar component |
| `GanttCalendarBackground.tsx` | Calendar background with working day indicators |
| `ConfirmDeleteModal.tsx` | Modal dialog for task deletion confirmation |
| `index.ts` | Module exports |

---

## GanttChart.tsx

### Purpose

Main container component that orchestrates the Gantt chart visualization. Manages state for tasks, dependencies, and handles user interactions.

### Key Functions/Components

```typescript
export const GanttChart = () => { ... }
```

**State:**
- `allTasks: GanttTask[]` - All project tasks
- `dependencies: GanttDependencyDto[]` - Task dependencies
- `pendingDeleteTaskId: string | null` - ID of task pending deletion

**Hooks Used:**
- `useTimelineCalendar()` - Fetch work calendar
- `useProjectPlan()` - Fetch project plan
- `useCreateProjectTask()` - Create new tasks
- `useChangeTaskStartDate()` - Update task start
- `useChangeTaskEndDate()` - Update task end
- `useCreateDependency()` - Create dependencies
- `useUpdateProjectTask()` - Update task details
- `useDeleteProjectTask()` - Delete tasks
- `useAssignTaskSchedule()` - Assign schedule to pool task
- `usePlanFromEnd()` - Plan task backwards from end date

**Event Handlers:**
- `handleUpdateTask(cmd: TaskCommand)` - Process task commands
- `handleCreateDependency(fromId, toId)` - Create task dependency
- `handleRemoveDependency(fromId, toId)` - Remove task dependency (local only)
- `handleConfirmDelete()` - Confirm task deletion
- `syncScroll()` - Synchronize scroll between panels
- `handleDragEnd(event)` - Handle drag-and-drop from pool to timeline
- `handleMoveTask(taskId, newStartDate)` - Move task bar
- `handleResizeTask(taskId, newEndDate)` - Resize task bar

**Drag-and-Drop:**
- Uses `@dnd-kit/react` `DragDropProvider` for pool-to-timeline drag
- Pool tasks can be dragged to `GanttCalendarGrid` drop zone
- Drop position calculates date based on column offset

### Dependencies

**Imports:**
- React hooks: `useState`, `useRef`, `useCallback`, `useEffect`
- Custom hooks: `useTimelineCalendar`, `useProjectTasks` (multiple)
- Types: `GanttTask`, `GanttDependencyDto`, `TaskCommand`, `GanttProjectPlan`
- Utils: `getCalendarRange`, `getDaysInRange`, `groupDaysByMonth`
- Child components: `GanttTaskList`, `GanttCalendarHeader`, `GanttCalendarGrid`, `ConfirmDeleteModal`

**Imported by:**
- `src/app/page.tsx`

### Usage Example

```tsx
// In app/page.tsx
import { GanttChart } from "@/components/GanttChart";

export default function Home() {
    return <GanttChart />
}
```

---

## GanttTaskList.tsx

### Purpose

Renders the left panel showing task titles in a scrollable list. Synchronized with the calendar grid scroll.

### Props

```typescript
interface GanttTaskListProps {
    tasks: GanttTask[]
    headerHeight: number
    rowHeight: number
    onUpdateTask: (cmd: TaskCommand) => void
}
```

### Dependencies

**Imports:**
- Types from `@/types`
- Task component for rendering

---

## GanttCalendarHeader.tsx

### Purpose

Renders the calendar header with month groups and day labels. Shows working day indicators based on the work calendar.

### Props

```typescript
interface GanttCalendarHeaderProps {
    days: string[]
    monthGroups: { month: string; days: number }[]
    dayWidth: number
    headerHeight: number
    workCalendar: WorkCalendar
}
```

### Dependencies

**Imports:**
- `getDayClasses` from utils
- Work calendar types

---

## GanttCalendarGrid.tsx

### Purpose

Main grid component rendering task rows, task bars, and dependency lines. Handles drag-and-drop interactions.

### Props

```typescript
interface GanttCalendarGridProps {
    tasks: GanttTask[]
    dependencies: GanttDependencyDto[]
    days: string[]
    rangeStart: string
    dayWidth: number
    rowHeight: number
    timelineCalendar: WorkCalendar
    onCreateDependency: (fromId: string, toId: string) => void
    onRemoveDependency: (fromId: string, toId: string) => void
    onMoveTask: (taskId: string, newStartDate: string) => void
    onResizeTask: (taskId: string, newEndDate: string) => void
}
```

### Dependencies

**Imports:**
- `GanttTaskRow` component
- Dependency rendering utilities
- Drag-and-drop context from @dnd-kit

---

## GanttTaskRow.tsx

### Purpose

Renders a single task row in the calendar grid, including the task bar and dependency connection points.

### Props

```typescript
interface GanttTaskRowProps {
    task: GanttTask
    days: string[]
    rangeStart: string
    dayWidth: number
    rowHeight: number
    onCreateDependency: (fromId: string, toId: string) => void
    onRemoveDependency: (fromId: string, toId: string) => void
    onUpdateTask: (cmd: TaskCommand) => void
}
```

### Features

- **Task Bar Rendering:** Displays the task bar at the correct position
- **Dependency Handles:** Connection points for creating/removing dependencies
- **Delete Button:** Hover-activated Trash2 icon for task deletion
- **Group Hover Pattern:** Delete button only visible on parent row hover
- **Drag-and-Drop:** Pool tasks use `useDraggable` from @dnd-kit for timeline assignment
- **Date Pickers:** PrimeReact Calendar for start/end date selection
- **Plan from End:** Right-click or special action on end date triggers backward planning

### Delete Button Implementation

```typescript
<button
    onClick={() => onUpdateTask({ type: TaskCommandType.DeleteTask, taskId: TaskId(task.id) })}
    className="opacity-0 group-hover:opacity-100 shrink-0 text-gray-400 hover:text-red-500 transition-opacity"
>
    <Trash2 size={14} />
</button>
```

**Styling:**
- Parent row has `group` class
- Button has `opacity-0 group-hover:opacity-100` for hover effect
- Red color on hover to indicate destructive action

### Drag-and-Drop Integration

```typescript
const { ref: dragRef, isDragging } = useDraggable({
    id: task.id,
    element: elementRef,
    data: { taskId: task.id },
    disabled: !isPoolTask,  // Only pool tasks are draggable
})
```

**Behavior:**
- Pool tasks (no start/end dates) are draggable
- Scheduled tasks cannot be dragged (use date pickers instead)
- Drag cursor shown only for draggable tasks

---

## GanttTaskLayer.tsx

### Purpose

SVG layer for rendering task bars and dependency lines.

### Props

```typescript
interface GanttTaskLayerProps {
    tasks: GanttTask[]
    dependencies: GanttDependencyDto[]
    days: string[]
    rangeStart: string
    dayWidth: number
    rowHeight: number
}
```

---

## GanttBar.tsx

### Purpose

Renders an individual task bar with drag handles for start/end date adjustment.

### Props

```typescript
interface GanttBarProps {
    task: GanttTask
    dayWidth: number
    rowHeight: number
}
```

---

## GanttCalendarBackground.tsx

### Purpose

Renders the calendar background with working day/non-working day visual indicators.

### Props

```typescript
interface GanttCalendarBackgroundProps {
    days: string[]
    dayWidth: number
    rowHeight: number
    workCalendar: WorkCalendar
}
```

---

## index.ts

### Purpose

Module barrel file exporting all GanttChart components.

```typescript
export { GanttChart } from './GanttChart'
export { GanttTaskList } from './GanttTaskList'
export { GanttCalendarHeader } from './GanttCalendarHeader'
export { GanttCalendarGrid } from './GanttCalendarGrid'
export { GanttTaskRow } from './GanttTaskRow'
export { GanttTaskLayer } from './GanttTaskLayer'
export { GanttBar } from './GanttBar'
export { GanttCalendarBackground } from './GanttCalendarBackground'
export { ConfirmDeleteModal } from './ConfirmDeleteModal'
```

---

## ConfirmDeleteModal.tsx

### Purpose

Modal dialog component that confirms task deletion. Displays the task title and the number of affected dependencies.

### Props

```typescript
interface ConfirmDeleteModalProps {
    taskTitle: string
    affectedDependenciesCount: number
    onConfirm: () => void
    onCancel: () => void
}
```

### Features

- **Task Title Display:** Shows the title of the task being deleted
- **Dependency Warning:** Displays count of dependencies that will be deleted
- **Confirmation Actions:** Cancel and Delete buttons
- **Styled with Tailwind:** Dark mode support, red delete button

### Usage Example

```tsx
<ConfirmDeleteModal
    taskTitle={pendingDeleteTask.title}
    affectedDependenciesCount={affectedDepsCount}
    onConfirm={handleConfirmDelete}
    onCancel={() => setPendingDeleteTaskId(null)}
/>
```

### Dependencies

**Imports:**
- React (no hooks - presentational component)

**Imported by:**
- `GanttChart.tsx`

---

## Task Commands

The GanttChart component uses a command pattern for type-safe task operations. Commands are dispatched via `handleUpdateTask()`:

### MoveTask

**Purpose:** Move a scheduled task to a new start date (maintaining duration).

**Command:**
```typescript
{
    type: TaskCommandType.MoveTask,
    taskId: string,
    newStartDate: string  // ISO date
}
```

**Trigger:** Drag task bar horizontally on timeline

**Handler:** `changeStartMutation` with optimistic update + rollback

---

### ResizeTask

**Purpose:** Change a task's end date (changing duration).

**Command:**
```typescript
{
    type: TaskCommandType.ResizeTask,
    taskId: string,
    newEndDate: string  // ISO date
}
```

**Trigger:** Drag resize handle on right edge of task bar

**Handler:** `changeEndMutation` with optimistic update + rollback

---

### AssignSchedule

**Purpose:** Assign a schedule to a pool task (drag from pool to timeline).

**Command:**
```typescript
{
    type: TaskCommandType.AssignSchedule,
    taskId: string,
    start: string,       // ISO date
    duration: number     // days
}
```

**Trigger:** Drop pool task onto timeline grid

**Handler:** `assignScheduleMutation` with optimistic update + rollback

---

### PlanFromEnd

**Purpose:** Plan a task backwards from its end date (deadline-driven).

**Command:**
```typescript
{
    type: TaskCommandType.PlanFromEnd,
    taskId: string,
    newEndDate: string  // ISO date (deadline)
}
```

**Trigger:** Right-click or special action on end date picker

**Handler:** `planFromEndMutation` — calculates start date backwards

---

## Related Files

- [Task Components](./components-task.md)
- [Block Components](./components-blocks.md)
- [Hooks](./hooks.md)
- [Types](./types.md)
