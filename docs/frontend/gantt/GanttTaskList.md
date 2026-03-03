# GanttTaskList

**Path:** `src/components/GanttChart/GanttTaskList.tsx`

## Purpose

Left panel displaying task names, start/end dates, and "Add task" button.

## Props

```typescript
interface GanttTaskListProps {
  tasks: Task[]
  headerHeight: number
  rowHeight: number
  onUpdateTask: (cmd: TaskCommand) => void
}
```

## Responsibilities

- Renders header with column labels (Task, Start, End)
- Renders `GanttTaskRow` for each task
- Provides "Add task" button that opens inline creation mode
- Handles inline task creation with title input

## Children

- `GanttTaskRow` — One per task

## Parent

- `GanttChart`

## Inline Task Creation

Clicking "Add task" button opens an inline input row:
- User enters task title (required, max 255 characters)
- **Enter** — creates task with `CreateTaskInPool` command
- **Escape** — cancels creation
- **Blur** (with empty input) — cancels creation

Visual feedback:
- Blue background highlight during creation
- Status indicator (gray circle) shown
- Auto-focused input field

## Command Pattern

All mutations go through single `onUpdateTask` callback:

```typescript
// Create task with title (inline creation)
onUpdateTask({ 
  type: TaskCommandType.CreateTaskInPool, 
  title: string 
})

// Delegated from GanttTaskRow:
// - UpdateTitle
// - ChangeStartDate
// - ChangeEndDate
```
