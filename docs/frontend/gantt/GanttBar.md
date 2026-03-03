# GanttBar

**Path:** `src/components/GanttChart/GanttBar.tsx`

## Purpose

Visual representation of a task on the timeline.

## Props

```typescript
interface GanttBarProps {
  task: Task
  rangeStart: Date
  dayWidth: number
  onLinkStart?: (taskId: string, e: React.MouseEvent) => void
  isLinkTarget?: boolean
}
```

## Responsibilities

- Displays task bar at correct position and width
- Shows task title
- Provides link handles for dependency creation

## Positioning

```typescript
const startOffset = getDayOffset(task.plannedStartDate, rangeStart)
const endOffset = getDayOffset(task.plannedEndDate, rangeStart)
const left = startOffset * dayWidth
const width = (endOffset - startOffset + 1) * dayWidth
```

## Features

| Feature | Description |
|---------|-------------|
| Status colors | Bar color based on task status |
| Right handle | Visible on hover — start linking |
| Left handle | Visible when target — link completion |

## Parent

- `GanttTaskLayer`

## Styles

- `ganttBar` — tailwind-variants variant
