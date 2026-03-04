# Block Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/`  
**Module:** [Frontend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

Block components provide drag-and-drop functionality for the Gantt chart using @dnd-kit. They enable users to create tasks by dragging from a pool and dropping onto the timeline.

## Files Overview

| File | Purpose |
|------|---------|
| `Block.tsx` | Base block component |
| `DraggableBlock.tsx` | Draggable wrapper component |
| `DroppableBlock.tsx` | Droppable zone wrapper |
| `GantElement.tsx` | Gantt-specific element |
| `MyBlock.tsx` | Custom styled block |
| `DefaultZone.tsx` | Default drop zone |

---

## Block.tsx

### Purpose

Base block component providing common styling and structure for draggable items.

### Component Signature

```typescript
interface BlockProps {
    children: React.ReactNode
    className?: string
    onClick?: () => void
}

export const Block: React.FC<BlockProps> = ({ children, className, onClick }) => { ... }
```

### Dependencies

**Imports:**
- React: `React.ReactNode`
- Tailwind CSS classes

**Imported by:**
- `DraggableBlock.tsx`
- `MyBlock.tsx`

---

## DraggableBlock.tsx

### Purpose

Wraps content with @dnd-kit draggable functionality.

### Component Signature

```typescript
interface DraggableBlockProps {
    id: string
    children: React.ReactNode
    data?: Record<string, unknown>
}

export const DraggableBlock: React.FC<DraggableBlockProps> = ({ id, children, data }) => { ... }
```

### Key Features

- Uses `useDraggable` from @dnd-kit
- Provides drag handle visual feedback
- Transfers data on drag

### Dependencies

**Imports:**
- `useDraggable` from `@dnd-kit/react`
- `Block` component

**Imported by:**
- Task pool components
- Gantt chart drop zones

---

## DroppableBlock.tsx

### Purpose

Creates a droppable zone for accepting dragged items.

### Component Signature

```typescript
interface DroppableBlockProps {
    id: string
    children: React.ReactNode
    onDrop?: (data: unknown) => void
}

export const DroppableBlock: React.FC<DroppableBlockProps> = ({ id, children, onDrop }) => { ... }
```

### Key Features

- Uses `useDroppable` from @dnd-kit
- Visual feedback on drag over
- Handles drop events

### Dependencies

**Imports:**
- `useDroppable` from `@dnd-kit/react`
- `Block` component

---

## GantElement.tsx

### Purpose

Specialized component for Gantt chart elements with drag-and-drop support.

### Component Signature

```typescript
interface GantElementProps {
    taskId: string
    startDate: string
    endDate: string
    children: React.ReactNode
}

export const GantElement: React.FC<GantElementProps> = ({ taskId, startDate, endDate, children }) => { ... }
```

### Dependencies

**Imports:**
- @dnd-kit hooks
- Date utility functions

---

## MyBlock.tsx

### Purpose

Custom styled block with application-specific styling.

### Component Signature

```typescript
interface MyBlockProps {
    title: string
    description?: string
    onClick?: () => void
}

export const MyBlock: React.FC<MyBlockProps> = ({ title, description, onClick }) => { ... }
```

### Dependencies

**Imports:**
- `Block` component
- Tailwind CSS classes

---

## DefaultZone.tsx

### Purpose

Default drop zone component for areas without specific drop handlers.

### Component Signature

```typescript
interface DefaultZoneProps {
    children: React.ReactNode
}

export const DefaultZone: React.FC<DefaultZoneProps> = ({ children }) => { ... }
```

### Dependencies

**Imports:**
- `DroppableBlock` component

---

## Related Files

- [GanttChart Components](./components-gantt-chart.md)
- [Task Components](./components-task.md)
- [Hooks](./hooks.md)
