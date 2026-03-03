# GanttTaskRow

**Path:** `src/components/GanttChart/GanttTaskRow.tsx`

## Purpose

Single row displaying task title and editable date fields.

## Props

```typescript
interface GanttTaskRowProps {
  task: Task
  rowHeight: number
  onUpdateTask: (cmd: TaskCommand) => void
}
```

## Responsibilities

- Displays status indicator (colored dot)
- Shows task title (click-to-edit)
- Shows start/end dates with calendar pickers
- Dispatches commands on user actions

## Features

| Feature | Description |
|---------|-------------|
| Status indicator | Colored dot based on task status |
| Inline title edit | Click to edit, Enter/Escape to confirm/cancel |
| Date pickers | PrimeReact Calendar with Russian locale |
| Commands | `UpdateTitle`, `ChangeStartDate`, `ChangeEndDate` |

## Parent

- `GanttTaskList`

## Dependencies

- `primereact/calendar` — Date selection popup
