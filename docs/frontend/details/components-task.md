# Task Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/Task/`  
**Module:** [Frontend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

## Purpose

The Task module provides the task row component used in the Gantt chart task list. It supports inline editing of task titles and displays task status.

## Files Overview

| File | Purpose |
|------|---------|
| `Task.tsx` | Main task row component with editing |
| `Task.styles.ts` | Tailwind-based style definitions |
| `index.ts` | Module exports |

---

## Task.tsx

### Purpose

Renders a single task row in the task list with inline title editing capability.

### Component Signature

```typescript
interface TaskProps {
    task: GanttTask
    onUpdateTask: (cmd: TaskCommand) => void
}

export const Task: React.FC<TaskProps> = ({ task, onUpdateTask }) => { ... }
```

### Key Features

- **Inline Editing:** Click to edit task title
- **Status Display:** Shows task status indicator
- **Command Dispatch:** Sends `UpdateTitle` command on change

### State Management

```typescript
const [isEditing, setIsEditing] = useState(false)
const [title, setTitle] = useState(task.title)
```

### Event Handlers

- `handleKeyDown(e: KeyboardEvent)` - Handle Enter/Escape during editing
- `handleBlur()` - Save changes on blur
- `handleClick()` - Enter edit mode

### Dependencies

**Imports:**
- React: `useState`, `KeyboardEvent`
- Types: `GanttTask`, `TaskCommand`, `TaskCommandType`
- Styles: `taskStyles` from `Task.styles.ts`
- Icons: `Pencil`, `Check`, `X` from lucide-react

**Imported by:**
- `GanttTaskList.tsx`

### Usage Example

```tsx
import { Task } from '@/components/Task'

<Task 
    task={ganttTask}
    onUpdateTask={(cmd) => handleUpdateTask(cmd)}
/>
```

---

## Task.styles.ts

### Purpose

Defines Tailwind CSS class names for the Task component styling.

### Exports

```typescript
export const taskStyles = {
    container: "flex items-center gap-2 px-3 py-2 hover:bg-gray-50 dark:hover:bg-zinc-900",
    title: "flex-1 text-sm text-gray-900 dark:text-gray-100",
    titleInput: "flex-1 text-sm bg-transparent border-b border-blue-500 focus:outline-none",
    status: "w-2 h-2 rounded-full",
    editButton: "p-1 hover:bg-gray-200 dark:hover:bg-zinc-800 rounded",
    // ... status-specific styles
}
```

### Dependencies

**Imports:**
- `twMerge` from tailwind-merge (if used)

**Imported by:**
- `Task.tsx`

---

## index.ts

### Purpose

Module barrel file exporting Task components.

```typescript
export { Task } from './Task'
export { taskStyles } from './Task.styles'
```

---

## Related Files

- [GanttChart Components](./components-gantt-chart.md)
- [Block Components](./components-blocks.md)
- [Types](./types.md)
