# ResourceLoad Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/ResourceLoad/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-15

## Purpose

The ResourceLoad module provides components for visualizing resource workload and managing resource leveling. It displays a heat map of resource allocation vs. capacity and provides tools to resolve overloads.

## Files Overview

| File | Purpose |
|------|---------|
| `ResourceLoadPage.tsx` | Main page showing resource load heat map |
| `LevelingPreviewDialog.tsx` | Dialog showing leveling preview before applying |
| `DayOverrideEditor.tsx` | Editor for overriding daily capacity |

---

## ResourceLoadPage.tsx

### Purpose

Main page component for visualizing resource load across all resources. Shows a heat map grid with color-coded allocation levels and provides access to leveling tools.

### Features

- **Resource Load Heat Map:** Visual grid showing daily allocation per resource
- **Color Coding:**
  - Blue (bg-blue-200): Normal load (< 80% capacity)
  - Amber (bg-amber-300): High load (80-100% capacity)
  - Red (bg-red-400): Overloaded (> 100% capacity)
- **Overload Indicators:** Warning triangle icon on overloaded days
- **Effort Deficit Badge:** Shows total effort deficit per resource
- **Date Range:** Automatically calculated from project plan tasks
- **Day Override Editor:** Click any day to edit capacity override
- **Leveling Preview:** Button to preview automatic leveling
- **Leveling Apply:** Button to apply leveling changes

### Component Structure

```typescript
export function ResourceLoadPage()
```

**State:**
- `levelingResult: LevelingResult | null` - Preview result from leveling
- `showLevelingDialog: boolean` - Controls LevelingPreviewDialog visibility
- `editingOverride: { resourceId: string; date: string } | null` - Day being edited

**Hooks Used:**
- `useResources()` - Fetch all resources
- `useAssignments()` - Fetch all assignments
- `useResourceLoad(from, to)` - Fetch resource load report
- `useProjectPlan()` - Fetch project plan for date range
- `useLevelingPreview()` - Preview leveling result
- `useApplyLeveling()` - Apply leveling changes

### Helper Functions

```typescript
getDateRange(plan): { from: string | null; to: string | null }
```
Calculates the min/max date range from all task start/end dates.

```typescript
getDaysArray(from, to): string[]
```
Generates array of ISO date strings between two dates.

### Sub-Components

#### DayCell

Renders a single day cell in the heat map grid.

**Props:**
- `day: ResourceDayLoad | undefined` - Load data for the day
- `date: string` - ISO date string
- `dayWidth: number` - Cell width in pixels
- `onClick: (date, e) => void` - Click handler

**Visual States:**
- Empty (no assignments): Light gray, hover effect
- Normal load: Blue bar with height proportional to load
- High load: Amber bar
- Overloaded: Red bar with warning triangle

#### ResourceRow

Renders a row for a single resource with name and daily cells.

**Props:**
- `result: ResourceLoadResult` - Load data for the resource
- `allDays: string[]` - All dates in range
- `dayWidth: number` - Cell width
- `onDayClick: (resourceId, date, e) => void` - Click handler

**Features:**
- Sticky resource name column (left)
- Effort deficit badge if applicable
- Day cells with color-coded load

### UI Structure

```
ResourceLoadPage
├── Header
│   ├── Back Arrow (Link to "/")
│   ├── Title: "Загрузка ресурсов"
│   └── Leveling Buttons
│       ├── Preview Button
│       └── Apply Button
├── Summary (optional)
│   ├── Total Overloaded Days
│   └── Total Effort Deficit
├── Heat Map Grid
│   ├── Header Row (dates)
│   └── Resource Rows
│       ├── Resource Name (sticky)
│       ├── Effort Deficit Badge (if applicable)
│       └── Day Cells (color-coded)
└── LevelingPreviewDialog (modal)
```

### Dependencies

**Imports:**
- React hooks: `useMemo`, `useState`
- Icons: `ArrowLeft`, `AlertTriangle`, `Wand2` from `lucide-react`
- `Link` from `next/link`
- Hooks: `useResources`, `useAssignments`, `useResourceLoad`, `useLevelingPreview`, `useApplyLeveling`, `useProjectPlan`
- Types: `ResourceLoadResult`, `ResourceDayLoad`, `LevelingResult`
- Components: `LevelingPreviewDialog`, `DayOverrideEditor`

**Imported by:**
- App router page (likely `src/app/resource-load/page.tsx`)

---

## LevelingPreviewDialog.tsx

### Purpose

Dialog component that displays the preview of resource leveling results before applying changes.

### Props

```typescript
interface LevelingPreviewDialogProps {
    open: boolean
    onClose: () => void
    result: LevelingResult | null
    onApply: () => void
}
```

### Features

- **Schedule Changes Table:** Lists all tasks that will be rescheduled
- **Overload Summary:** Shows resolved vs. remaining overloads
- **Apply/Cancel Actions:** Buttons to apply or discard changes

### LevelingResult Structure

```typescript
interface LevelingResult {
    updatedSchedules: Array<{
        taskId: string
        start: string
        end: string
    }>
    resolvedOverloads: number
    remainingOverloads: number
}
```

### Dependencies

**Imports:**
- React hooks
- PrimeReact Dialog (or similar)
- Types from schemas

**Imported by:**
- `ResourceLoadPage.tsx`

---

## DayOverrideEditor.tsx

### Purpose

Popover/dialog component for editing capacity overrides on specific days. Allows users to set custom availability for a resource on a given date.

### Props

```typescript
interface DayOverrideEditorProps {
    resourceId: string
    date: string
    currentHours?: number
    onClose: () => void
    onSave: (hours: number) => void
}
```

### Features

- **Date Display:** Shows the date being edited
- **Hours Input:** Number field for available hours
- **Clear Override:** Option to remove existing override
- **Save/Cancel Actions:** Buttons to confirm or discard

### Use Cases

- **Vacation:** Set hours to 0 for vacation days
- **Part-time:** Reduce hours for partial availability
- **Extra capacity:** Increase hours for overtime days

### Dependencies

**Imports:**
- React hooks: `useState`
- Icons from `lucide-react`
- Hooks: `useSetDayOverride`, `useDeleteDayOverride`

**Imported by:**
- `ResourceLoadPage.tsx`

---

## Color Coding Reference

| Load Ratio | Color | Meaning |
|------------|-------|---------|
| 0% | (none) | No assignments |
| 1-80% | Blue (bg-blue-200) | Normal load |
| 80-100% | Amber (bg-amber-300) | High load |
| >100% | Red (bg-red-400) | Overloaded |

---

## Related Files

- [Resources Components](./components-resources.md)
- [Assignments Components](./components-assignments.md)
- [Hooks](./hooks.md#useAssignments)
- [Services](./services.md#assignmentsApi)
- [Types](./types.md#assignment-schema)
