# GanttCalendarBackground

**Path:** `src/components/GanttChart/GanttCalendarBackground.tsx`

## Purpose

Renders background cells for each day (working/non-working, today highlight).

## Props

```typescript
interface GanttCalendarBackgroundProps {
  days: Date[]
  rowHeight: number
  totalRows: number
  dayWidth: number
  timelineCalendar: TimelineCalendar
}
```

## Responsibilities

- Renders a column for each day in the range
- Applies working/non-working day styles
- Highlights today

## Rendering

- Absolute positioned columns
- Full height (spans all task rows)
- Uses `ganttDayCell` style variant

## Parent

- `GanttCalendarGrid`

## Styles

- `ganttDayCell` — tailwind-variants variant
