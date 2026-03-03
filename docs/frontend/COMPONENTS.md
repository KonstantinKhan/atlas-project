# Frontend Components

Map of all React components in the Atlas Project frontend application.

**Location:** `frontend/atlas-project-web-app/src/components/`

---

## GanttChart Components

```
GanttChart
├── GanttTaskList
│   └── GanttTaskRow
└── GanttCalendarGrid
    ├── GanttCalendarHeader
    ├── GanttCalendarBackground
    └── GanttTaskLayer
        └── GanttBar
```

| Component               | Purpose                 | Details                                       |
| ----------------------- | ----------------------- | --------------------------------------------- |
| GanttChart              | Root orchestrator       | [Details](./gantt/GanttChart.md)              |
| GanttTaskList           | Task list panel (left)  | [Details](./gantt/GanttTaskList.md)           |
| GanttTaskRow            | Single task row         | [Details](./gantt/GanttTaskRow.md)            |
| GanttCalendarGrid       | Calendar grid container | [Details](./gantt/GanttCalendarGrid.md)       |
| GanttCalendarHeader     | Month/day header        | [Details](./gantt/GanttCalendarHeader.md)     |
| GanttCalendarBackground | Day background cells    | [Details](./gantt/GanttCalendarBackground.md) |
| GanttTaskLayer          | Task bars layer         | [Details](./gantt/GanttTaskLayer.md)          |
| GanttBar                | Task bar on timeline    | [Details](./gantt/GanttBar.md)                |

---

## Task Components

| Component | Purpose           | Details                       |
| --------- | ----------------- | ----------------------------- |
| TaskCard  | Task card display | [Details](./task/TaskCard.md) |

---

## Other Components

| Component      | Purpose            |
| -------------- | ------------------ |
| Block          | Drag-and-drop demo |
| DraggableBlock | Draggable wrapper  |
| DroppableBlock | Droppable zone     |
| DefaultZone    | Default drop zone  |
| GantElement    | Experimental       |
| MyBlock        | Experimental       |

---

## Patterns

- [Command Type Pattern](../../docs/rules/command-type-pattern.md) — All mutations use typed commands
- **Inline Task Creation** — `GanttTaskList` provides inline input for creating tasks with required title

---

## Conventions

- **Styling:** tailwind-variants (`*.styles.ts`)
- **Naming:** `PascalCase.tsx` for components
- **Types:** `*.type.ts` for type definitions
