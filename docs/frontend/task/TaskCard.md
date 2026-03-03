# TaskCard

**Path:** `src/components/Task/Task.tsx`

## Purpose

Display a task as a card with status, dates, and duration.

## Props

```typescript
interface TaskProps {
  task: Task
}
```

## Responsibilities

- Shows task status with color-coded indicator
- Displays task ID and title
- Shows description
- Lists metadata: duration, planned dates

## Features

| Feature | Description |
|---------|-------------|
| Status indicator | Colored bar on the left |
| ID badge | `#taskId` format |
| Status badge | Human-readable status label |
| Duration | Shows planned calendar duration |
| Dates | Start and end dates with icons |

## Icons

- `Clock` — Duration
- `Calendar` — Start date
- `CalendarCheck2` — End date

## Styles

- `taskCard` — Main card container (variant by status)
- `statusIndicator` — Left status bar
- `statusBadge` — Status label badge
- `taskIdBadge` — ID badge
- `taskTitle` — Title typography
- `taskDescription` — Description text
- `taskMetadata` — Metadata container
- `taskMetadataItem` — Single metadata item

## Dependencies

- `lucide-react` — Icons
- `tailwind-variants` — Styling
