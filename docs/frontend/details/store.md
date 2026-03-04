# Store - Detail

**Path:** `/frontend/atlas-project-web-app/src/store/`  
**Module:** [Frontend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

Zustand-based state management for UI-related state that doesn't require server synchronization. Provides a lightweight alternative to React Query for client-only state.

## Files Overview

| File | Purpose |
|------|---------|
| `timelineCalendarStore.ts` | UI state for calendar display settings |

---

## timelineCalendarStore.ts

### Purpose

Manages UI state for the timeline calendar display, including visibility toggles for holidays, weekends, and settings panel state.

### Store Interface

```typescript
interface CalendarUIState {
    showHolidays: boolean
    showWeekends: boolean
    isSettingsPanelOpen: boolean
}

interface TimelineCalendarStore {
    ui: CalendarUIState
    
    // Actions
    toggleHolidays: () => void
    toggleWeekends: () => void
    setSettingsPanelOpen: (open: boolean) => void
}
```

### Initial State

```typescript
{
    ui: {
        showHolidays: true,
        showWeekends: true,
        isSettingsPanelOpen: false
    }
}
```

### Actions

#### toggleHolidays()

**Purpose:** Toggle holiday visibility on the calendar.

**Usage:**
```typescript
const { toggleHolidays } = useTimelineCalendarStore()
toggleHolidays()
```

---

#### toggleWeekends()

**Purpose:** Toggle weekend display on the calendar.

**Usage:**
```typescript
const { toggleWeekends } = useTimelineCalendarStore()
toggleWeekends()
```

---

#### setSettingsPanelOpen(open: boolean)

**Purpose:** Open or close the settings panel.

**Parameters:**
- `open` - Whether to show the settings panel

**Usage:**
```typescript
const { setSettingsPanelOpen } = useTimelineCalendarStore()
setSettingsPanelOpen(true)  // Open panel
setSettingsPanelOpen(false) // Close panel
```

---

### Store Creation

```typescript
import { create } from 'zustand'

export const useTimelineCalendarStore = create<TimelineCalendarStore>((set) => ({
    ui: {
        showHolidays: true,
        showWeekends: true,
        isSettingsPanelOpen: false,
    },

    toggleHolidays: () =>
        set((state) => ({
            ui: { ...state.ui, showHolidays: !state.ui.showHolidays },
        })),

    toggleWeekends: () =>
        set((state) => ({
            ui: { ...state.ui, showWeekends: !state.ui.showWeekends },
        })),

    setSettingsPanelOpen: (open) =>
        set((state) => ({
            ui: { ...state.ui, isSettingsPanelOpen: open },
        })),
}))
```

### Dependencies

**Imports:**
- `create` from `zustand`

**Imported by:**
- Components that need to read/update calendar UI settings
- Settings panel component (when implemented)

---

## Usage Example

```typescript
import { useTimelineCalendarStore } from '@/store/timelineCalendarStore'

function CalendarSettings() {
    const { ui, toggleHolidays, toggleWeekends, setSettingsPanelOpen } = 
        useTimelineCalendarStore()
    
    return (
        <div>
            <label>
                <input 
                    type="checkbox" 
                    checked={ui.showHolidays}
                    onChange={toggleHolidays}
                />
                Show Holidays
            </label>
            <label>
                <input 
                    type="checkbox" 
                    checked={ui.showWeekends}
                    onChange={toggleWeekends}
                />
                Show Weekends
            </label>
            <button onClick={() => setSettingsPanelOpen(false)}>
                Close
            </button>
        </div>
    )
}
```

---

## Design Decisions

### Why Zustand?

- **Lightweight:** Minimal boilerplate compared to Redux
- **No Provider Needed:** Works without wrapping the app
- **Simple API:** Easy to understand and extend
- **Type-Safe:** Full TypeScript support

### Separation from React Query

| State Type | Management | Reason |
|------------|------------|--------|
| Server state (tasks, calendar) | React Query | Caching, refetch, mutations |
| UI state (visibility, panels) | Zustand | Client-only, no sync needed |

---

## Related Files

- [Hooks](./hooks.md)
- [GanttChart Components](./components-gantt-chart.md)
