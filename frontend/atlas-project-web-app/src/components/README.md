# Frontend Components

**Path:** `/frontend/atlas-project-web-app/src/components`  
**Last Updated:** 2026-03-09

## Overview

This directory contains all React components for the Atlas Project Gantt Chart application. Components are organized by feature, with each major feature having its own subdirectory.

## Component Structure

### GanttChart/ Directory

Main Gantt chart visualization components.

| Component | File | Purpose |
|-----------|------|---------|
| `GanttChart` | `GanttChart.tsx` | Main container component, orchestrates all Gantt functionality |
| `GanttTaskList` | `GanttTaskList.tsx` | Left panel showing task list with editable fields |
| `GanttCalendarGrid` | `GanttCalendarGrid.tsx` | Timeline grid with task bars and dependencies |
| `GanttCalendarHeader` | `GanttCalendarHeader.tsx` | Timeline header with month/week labels |
| `GanttTaskRow` | `GanttTaskRow.tsx` | Individual task row in the timeline |
| `GanttBar` | `GanttBar.tsx` | Visual task bar representation |
| `GanttTaskLayer` | `GanttTaskLayer.tsx` | Layer for rendering task bars |
| `GanttCalendarBackground` | `GanttCalendarBackground.tsx` | Background grid with working day highlighting |
| `DependencyActionPopover` | `DependencyActionPopover.tsx` | Popover for dependency actions |
| `DependencyTypePopover` | `DependencyTypePopover.tsx` | Popover for changing dependency type |
| `ConfirmDeleteModal` | `ConfirmDeleteModal.tsx` | Modal for confirming task deletion |
| `Toast` | `Toast.tsx` | Toast notification component |
| `index` | `index.ts` | Barrel export |

### Task/ Directory

Task-specific display components.

| Component | File | Purpose |
|-----------|------|---------|
| `Task` | `Task.tsx` | Task row component for task list |
| `Task.styles` | `Task.styles.ts` | Styled components for Task |
| `index` | `index.ts` | Barrel export |

### Root Components

Shared components at the components root level.

| Component | File | Purpose |
|-----------|------|---------|
| `Block` | `Block.tsx` | Generic block component |
| `DraggableBlock` | `DraggableBlock.tsx` | Draggable wrapper component |
| `DroppableBlock` | `DroppableBlock.tsx` | Droppable zone component |
| `GantElement` | `GantElement.tsx` | Base Gantt element |
| `MyBlock` | `MyBlock.tsx` | Custom block component |
| `DefaultZone` | `DefaultZone.tsx` | Default drop zone |

## Main Component: GanttChart

### Props

The `GanttChart` component takes no props and manages all internal state.

### State Management

```typescript
// View mode (day/week)
const viewMode = useTimelineCalendarStore((s) => s.ui.viewMode)

// All tasks (scheduled + unscheduled)
const [allTasks, setAllTasks] = useState<GanttTask[]>([])

// Task dependencies
const [dependencies, setDependencies] = useState<GanttDependencyDto[]>([])
```

### Key Features

1. **Optimistic Updates**: UI updates immediately, rolls back on error
2. **Command Pattern**: All task operations use `TaskCommand` type
3. **Scroll Sync**: Left panel and timeline scroll together
4. **Drag & Drop**: Full DnD support for task scheduling

### Task Commands

```typescript
enum TaskCommandType {
  CreateTaskInPool,
  ChangeStartDate,
  ChangeEndDate,
  UpdateTitle,
  UpdateDescription,
  ChangeStatus,
  DeleteTask,
  PlanFromEnd,
  AssignSchedule,
  MoveTask,
  ResizeTask,
}
```

## GanttCalendarGrid

### Props

```typescript
interface GanttCalendarGridProps {
  tasks: GanttTask[]
  dependencies: GanttDependencyDto[]
  days: Date[]
  rangeStart: Date
  dayWidth: number
  rowHeight: number
  timelineCalendar: TimelineCalendar
  viewMode: ViewMode
  criticalTaskIds?: Set<string>
  slackMap?: Map<string, number>
  onCreateDependency: (from: string, to: string, type: string) => void
  onChangeDependencyType: (from: string, to: string, type: string) => void
  onRemoveDependency: (from: string, to: string) => void
  onMoveTask: (taskId: string, newDate: string) => void
  onResizeTask: (taskId: string, newEndDate: string) => void
  onResizeFromStart: (taskId: string, newStartDate: string) => void
}
```

### Drop Zone

The grid includes a drop zone for scheduling unscheduled tasks:

```typescript
export const TIMELINE_DROP_ID = 'timeline-drop-zone'

// In handleDragEnd:
if (operation.target.id !== TIMELINE_DROP_ID) return
```

## GanttTaskList

### Features

- Displays all tasks in a scrollable list
- Inline editing for title, description, status
- Syncs scroll with calendar grid
- Shows task metadata (dates, duration)

### Props

```typescript
interface GanttTaskListProps {
  tasks: GanttTask[]
  headerHeight: number
  rowHeight: number
  onUpdateTask: (command: TaskCommand) => void
}
```

## Styling

Components use Tailwind CSS with custom styled-components for dynamic values:

```typescript
// Example from GanttChart.styles.ts
export const TaskBar = styled.div<{
  $start: number
  $width: number
  $isCritical: boolean
}>`
  left: ${({ $start }) => $start}px;
  width: ${({ $width }) => $width}px;
  background: ${({ $isCritical }) => $isCritical ? '#ef4444' : '#3b82f6'};
`
```

## Drag & Drop Integration

Components use `@dnd-kit/react` for drag and drop:

```tsx
<DragDropProvider onDragEnd={handleDragEnd}>
  <DraggableBlock id={task.id}>
    <Task {...task} />
  </DraggableBlock>
  <DroppableBlock id={TIMELINE_DROP_ID}>
    {/* Drop zone */}
  </DroppableBlock>
</DragDropProvider>
```

## Related Files

- [Hooks](../hooks/README.md) - Custom React hooks
- [Types](../types/README.md) - TypeScript types and schemas
- [Store](../store/README.md) - Zustand state management
