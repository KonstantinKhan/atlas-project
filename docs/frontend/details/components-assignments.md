# Assignments Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/Assignments/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-15

## Purpose

The Assignments module provides components for managing task-resource assignments. It includes a popover editor for assigning resources to tasks and managing their effort allocation.

## Files Overview

| File | Purpose |
|------|---------|
| `AssignmentEditor.tsx` | Popover editor for managing task assignments |

---

## AssignmentEditor.tsx

### Purpose

Popover component that appears when managing assignments for a specific task. Allows users to add/remove resources, adjust hours per day, and set planned effort.

### Props

```typescript
interface AssignmentEditorProps {
    taskId: string
    taskTitle: string
    onClose: () => void
    position: { x: number; y: number }
}
```

**Parameters:**
- `taskId` - ID of the task being edited
- `taskTitle` - Title of the task (displayed in header)
- `onClose` - Callback to close the popover
- `position` - Absolute position for popover placement

### Features

- **Current Assignments List:** Shows all resources assigned to the task
- **Hours Per Day:** Editable field for each assignment (0.5-24 hours)
- **Planned Effort:** Optional field for total effort hours
- **Add Resource:** Dropdown to select from available (unassigned) resources
- **Remove Assignment:** Delete button for each assignment
- **Empty State:** Shows "Нет назначений" when no assignments exist
- **Positioned Popover:** Appears at specified x,y coordinates

### Component Structure

```typescript
export function AssignmentEditor({ taskId, taskTitle, onClose, position }: AssignmentEditorProps)
```

**State:**
- `addingResourceId: string` - Currently selected resource ID for addition

**Hooks Used:**
- `useResources()` - Fetch all resources
- `useAssignments()` - Fetch all assignments
- `useCreateAssignment()` - Create new assignment
- `useUpdateAssignment()` - Update assignment (hours or effort)
- `useDeleteAssignment()` - Remove assignment

### Derived Data

```typescript
taskAssignments = allAssignments?.filter((a) => a.taskId === taskId)
assignedResourceIds = Set(taskAssignments.map((a) => a.resourceId))
availableResources = resources?.filter((r) => !assignedResourceIds.has(r.id))
```

### Handlers

```typescript
handleAdd()
```
Creates a new assignment for the selected resource. Clears selection after success.

```typescript
handleUpdateHours(assignment, hoursPerDay)
```
Updates hours per day for an assignment. Validates range (0.5-24).

```typescript
handleUpdateEffort(assignment, value)
```
Updates planned effort hours. Accepts empty string to clear, or number (0.5-9999).

```typescript
handleDelete(id)
```
Deletes an assignment.

```typescript
getResourceName(resourceId)
```
Looks up resource name from the resources list.

### UI Structure

```
AssignmentEditor Popover
├── Header
│   ├── Task Title (truncated)
│   └── Close Button (X icon)
├── Content
│   ├── Empty State: "Нет назначений" (if no assignments)
│   └── Assignments List (if assignments exist)
│       └── For each assignment:
│           ├── Resource Name
│           ├── Hours/Day Input (number, 0.5-24)
│           ├── "ч/д" label
│           ├── Delete Button (Trash2 icon)
│           └── Planned Effort Row
│               ├── "План:" label
│               ├── Effort Input (number, optional)
│               └── "ч" label
└── Add Resource Section
    ├── Resource Dropdown (available resources only)
    └── Add Button (Plus icon)
```

### Styling

- **Position:** Fixed positioning at `position.x`, `position.y`
- **Z-index:** 50 (above most content)
- **Width:** 288px (w-72)
- **Border:** Light gray border with shadow
- **Background:** White with dark mode support

### Dependencies

**Imports:**
- React hooks: `useState`
- Icons: `X`, `Plus`, `Trash2` from `lucide-react`
- Hooks: `useResources`, `useAssignments`, `useCreateAssignment`, `useUpdateAssignment`, `useDeleteAssignment`
- Types: `Resource`, `TaskAssignment` from schemas

**Imported by:**
- GanttChart components (likely `GanttTaskRow.tsx` or similar)

### Usage Example

```tsx
// In parent component
const [assignmentEditor, setAssignmentEditor] = useState<{
    taskId: string
    taskTitle: string
    position: { x: number; y: number }
} | null>(null)

// Open editor
setAssignmentEditor({
    taskId: task.id,
    taskTitle: task.title,
    position: { x: event.clientX, y: event.clientY }
})

// Render
{assignmentEditor && (
    <AssignmentEditor
        taskId={assignmentEditor.taskId}
        taskTitle={assignmentEditor.taskTitle}
        position={assignmentEditor.position}
        onClose={() => setAssignmentEditor(null)}
    />
)}
```

---

## Assignment Data Model

### TaskAssignment

```typescript
interface TaskAssignment {
    id: string
    taskId: string
    resourceId: string
    hoursPerDay: number
    plannedEffortHours?: number | null
}
```

**Fields:**
- `id` - Unique assignment identifier
- `taskId` - Reference to the task
- `resourceId` - Reference to the resource
- `hoursPerDay` - Daily allocation (0.5-24)
- `plannedEffortHours` - Optional total effort estimate

---

## Related Files

- [Resources Components](./components-resources.md)
- [ResourceLoad Components](./components-resource-load.md)
- [Hooks](./hooks.md#useAssignments)
- [Services](./services.md#assignmentsApi)
- [Types](./types.md#assignment-schema)
