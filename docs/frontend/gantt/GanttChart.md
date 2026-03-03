# GanttChart

**Path:** `src/components/GanttChart/GanttChart.tsx`

## Purpose

Root orchestrator component for the Gantt chart visualization.

## Props

None (self-contained component)

## Responsibilities

- Fetches calendar and project plan data via hooks
- Converts `GanttProjectPlan` to internal `Task[]` format
- Handles task mutations via `TaskCommand` pattern
- Manages dependency creation/removal between tasks
- Synchronizes scroll between left (task list) and right (calendar) panels

## Key Hooks

| Hook | Purpose |
|------|---------|
| `useTimelineCalendar` | Fetches work calendar configuration |
| `useProjectPlan` | Fetches project plan data |
| `useCreateProjectTask` | Creates new tasks |
| `useChangeTaskStartDate` | Updates task start date |
| `useChangeTaskEndDate` | Updates task end date |
| `useCreateDependency` | Creates task dependencies |

## Children

- `GanttTaskList` — Left panel with task names and dates
- `GanttCalendarGrid` — Right panel with calendar visualization

## Constants

```typescript
const DAY_COLUMN_WIDTH = 40
const ROW_HEIGHT = 48
const HEADER_HEIGHT = 60
```
