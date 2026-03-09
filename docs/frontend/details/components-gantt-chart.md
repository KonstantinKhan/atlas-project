# GanttChart Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/GanttChart/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-06

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
| `DependencyActionPopover.tsx` | Popover for dependency actions (change type, delete) |
| `DependencyTypePopover.tsx` | Popover for selecting dependency type during creation |
| `Toast.tsx` | Toast notification for error messages |
| `GanttChart.styles.ts` | Shared style constants |
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

**Helper Functions:**

- `syncPlanState(newPlan: GanttProjectPlan)` — Synchronizes local state after successful mutations. Updates `tasks` and `dependencies` from the new plan returned by the server. Ensures client state matches server state after mutations.

- `handleFieldUpdate(taskId: string, updates: Partial<GanttTask>, errorMessage: string)` — Unified pattern for optimistic field updates. Handles `UpdateTitle`, `UpdateDescription`, and `ChangeStatus` commands. Performs optimistic update, calls mutation, and rolls back to `prevTasksRef.current` on error.

- `optimisticMutation(taskId: string, updateFn: (tasks: GanttTask[]) => GanttTask[], mutationFn: () => Promise<void>)` — Generic optimistic mutation with rollback. Saves previous state to `prevTasksRef.current` before applying update. Executes mutation, rolls back on error. Used by `MoveTask`, `ResizeTask`, and `AssignSchedule` commands.

**Event Handlers:**
- `handleUpdateTask(cmd: TaskCommand)` - Process task commands using unified patterns:
  - Field updates (`UpdateTitle`, `UpdateDescription`, `ChangeStatus`) → `handleFieldUpdate`
  - Optimistic mutations (`MoveTask`, `ResizeTask`, `AssignSchedule`) → `optimisticMutation`
  - Unknown commands → logged via `console.error` (no error thrown)
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
- Types: `GanttTask`, `GanttDependencyDto`, `TaskCommand`, `GanttProjectPlan`, `ProjectTaskStatus`
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
export { DependencyActionPopover } from './DependencyActionPopover'
export { DependencyTypePopover } from './DependencyTypePopover'
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

## DependencyActionPopover.tsx

### Purpose

Popover component that appears when clicking on a dependency arrow. Provides actions to change the dependency type or delete the dependency.

### Props

```typescript
interface DependencyActionPopoverProps {
    fromTaskId: string
    toTaskId: string
    currentType: string  // FS, SS, FF, SF
    position: { x: number, y: number }
    onChangeType: (fromId: string, toId: string, newType: string) => void
    onDelete: (fromId: string, toId: string) => void
    onClose: () => void
}
```

### Features

- **Dependency Type Buttons:** Four buttons for FS, SS, FF, SF types
- **Delete Button:** Red delete button to remove the dependency
- **Positioned Popover:** Appears near the clicked dependency arrow
- **Click-Outside Dismiss:** Closes when clicking outside the popover
- **Escape Key Dismiss:** Closes on Escape key press

### Usage Example

```tsx
<DependencyActionPopover
    fromTaskId={clickedDep.fromTaskId}
    toTaskId={clickedDep.toTaskId}
    currentType={clickedDep.type}
    position={popoverPosition}
    onChangeType={handleChangeDependencyType}
    onDelete={handleDeleteDependency}
    onClose={() => setClickedDep(null)}
/>
```

### Dependencies

**Imports:**
- React hooks: `useEffect`, `useRef`
- Icons from `lucide-react`
- Tailwind for styling

**Imported by:**
- `GanttTaskLayer.tsx`

---

## DependencyTypePopover.tsx

### Purpose

Popover component for selecting dependency type when creating a new dependency via gesture (drag from one task to another).

### Props

```typescript
interface DependencyTypePopoverProps {
    fromTaskId: string
    toTaskId: string
    position: { x: number, y: number }
    onSelectType: (fromId: string, toId: string, type: string) => void
    onCancel: () => void
}
```

### Features

- **Type Selection:** Four buttons for FS, SS, FF, SF types
- **Gesture-Based Creation:** Appears after dragging from source task to target task
- **Positioned Popover:** Appears near the target task
- **Click-Outside Dismiss:** Closes when clicking outside

### Usage Example

```tsx
<DependencyTypePopover
    fromTaskId={dragStartTaskId}
    toTaskId={dragEndTaskId}
    position={popoverPosition}
    onSelectType={handleCreateDependency}
    onCancel={() => setDependencyCreation(null)}
/>
```

### Dependencies

**Imports:**
- React hooks: `useEffect`, `useRef`
- Tailwind for styling

**Imported by:**
- `GanttTaskLayer.tsx`

---

## Task Commands

The GanttChart component uses a command pattern for type-safe task operations. Commands are dispatched via `handleUpdateTask()`:

### UpdateTitle

**Purpose:** Update a task's title.

**Command:**
```typescript
{
    type: TaskCommandType.UpdateTitle,
    taskId: string,
    title: string
}
```

**Trigger:** User edits task title in the task list

**Handler:** Uses `handleFieldUpdate` with optimistic update + rollback on error.

---

### UpdateDescription

**Purpose:** Update a task's description.

**Command:**
```typescript
{
    type: TaskCommandType.UpdateDescription,
    taskId: string,
    description: string
}
```

**Trigger:** User edits task description

**Handler:** Uses `handleFieldUpdate` with optimistic update + rollback on error.

---

### ChangeStatus

**Purpose:** Change a task's status.

**Command:**
```typescript
{
    type: TaskCommandType.ChangeStatus,
    taskId: string,
    status: ProjectTaskStatus
}
```

**Trigger:** User changes task status via dropdown or action

**Handler:** Uses `handleFieldUpdate` with optimistic update + rollback on error.

---

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

**Handler:** Uses `optimisticMutation` with optimistic update + rollback on error. Saves previous state to `prevTasksRef.current` before applying update.

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

**Handler:** Uses `optimisticMutation` with optimistic update + rollback on error. Saves previous state to `prevTasksRef.current` before applying update.

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

**Handler:** Uses `optimisticMutation` with optimistic update + rollback on error. Saves previous state to `prevTasksRef.current` before applying update.

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
