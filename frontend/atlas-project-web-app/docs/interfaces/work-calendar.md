# WorkCalendar Interface

## Metadata
- **Kind:** TypeScript Interface
- **Name:** `WorkCalendar`
- **Location:** `src/types/interfaces/work-calendar.interface.ts`

## Purpose
Defines the working calendar configuration for the Gantt chart component, including weekend days, holidays, and working weekend exceptions.

## Fields

### `weekendDays`
- **Type:** `number[]`
- **Required:** Yes
- **Default:** `[0, 6]`
- **Description:** Days of the week considered non-working (0 = Sunday, 6 = Saturday)

### `holidays`
- **Type:** `string[]`
- **Required:** Yes
- **Format:** `YYYY-MM-DD`
- **Description:** Holiday dates that are non-working regardless of the day of the week

### `workingWeekends`
- **Type:** `string[]`
- **Required:** No
- **Format:** `YYYY-MM-DD`
- **Description:** Exception dates - weekend days that are working (e.g., rescheduled weekends)

## Example

```typescript
const calendar: WorkCalendar = {
  weekendDays: [0, 6],
  holidays: ['2026-01-01', '2026-05-09'],
  workingWeekends: ['2026-05-11'],
}
```

## Related
- Hook: `useWorkCalendar` (`src/hooks/useWorkCalendar.ts`)
- Store: `useWorkCalendarStore` (`src/store/workCalendarStore.ts`)
- API: `workCalendarApi` (`src/services/workCalendarApi.ts`)
