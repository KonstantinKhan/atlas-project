# GanttCalendarHeader

**Path:** `src/components/GanttChart/GanttCalendarHeader.tsx`

## Purpose

Month and day number header above the calendar grid.

## Props

```typescript
interface GanttCalendarHeaderProps {
  days: Date[]
  monthGroups: { month: string; year: number; count: number }[]
  dayWidth: number
  headerHeight: number
  workCalendar: TimelineCalendar
}
```

## Responsibilities

- Renders two-row header: months → day numbers
- Highlights today
- Marks non-working days

## Features

| Feature | Description |
|---------|-------------|
| Month row | Grouped months with year |
| Day row | Individual day numbers |
| Today highlight | Special styling for current day |
| Non-working marks | Visual distinction for weekends/holidays |

## Parent

- `GanttChart`

## Styles

- `ganttHeaderDay` — tailwind-variants variant
