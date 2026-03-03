# GanttCalendarGrid

**Path:** `src/components/GanttChart/GanttCalendarGrid.tsx`

## Purpose

Container for the calendar visualization (right panel).

## Props

```typescript
interface GanttCalendarGridProps {
  tasks: Task[]
  days: Date[]
  rangeStart: Date
  dayWidth: number
  rowHeight: number
  timelineCalendar: TimelineCalendar
  onCreateDependency: (fromId: string, toId: string) => void
  onRemoveDependency: (fromId: string, toId: string) => void
}
```

## Responsibilities

- Renders background layer (working/non-working days)
- Renders task bars layer
- Handles drag-to-create dependency interactions

## State

| State | Type | Description |
|-------|------|-------------|
| `linkingFrom` | `string \| null` | Task ID being linked from |
| `mousePos` | `{ x, y }` | Current mouse position for link line |

## Children

- `GanttCalendarBackground` — Static background cells
- `GanttTaskLayer` — Interactive task bars

## Parent

- `GanttChart`
