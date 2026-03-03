# GanttTaskLayer

**Path:** `src/components/GanttChart/GanttTaskLayer.tsx`

## Purpose

Renders task bars and handles dependency linking interactions.

## Props

```typescript
interface GanttTaskLayerProps {
  tasks: Task[]
  days: Date[]
  rangeStart: Date
  dayWidth: number
  rowHeight: number
  onCreateDependency: (fromId: string, toId: string) => void
  onRemoveDependency: (fromId: string, toId: string) => void
  linkingFrom: string | null
  setLinkingFrom: (id: string | null) => void
  mousePos: { x: number; y: number }
  setMousePos: (pos: { x: number; y: number }) => void
}
```

## Responsibilities

- Renders `GanttBar` for each task
- Handles mouse events for dependency creation
- Draws link line from source task to mouse position
- Detects drop target on mouse up

## Features

| Feature | Description |
|---------|-------------|
| Task bars | Renders bars at correct positions |
| Link start | Click handle on bar to start linking |
| Link preview | Draws line to cursor position |
| Link complete | Release on target bar to create dependency |

## Children

- `GanttBar` — One per task

## Parent

- `GanttCalendarGrid`
