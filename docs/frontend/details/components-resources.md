# Resources Components - Detail

**Path:** `/frontend/atlas-project-web-app/src/components/Resources/`
**Module:** [Frontend Index](../INDEX.md)
**Last Updated:** 2026-03-15

## Purpose

The Resources module provides components for managing project resources (people and roles). It includes a resources page with CRUD operations and a dialog for creating new resources.

## Files Overview

| File | Purpose |
|------|---------|
| `ResourcesPage.tsx` | Main resources management page with table view |
| `CreateResourceDialog.tsx` | Dialog for creating new resources |

---

## ResourcesPage.tsx

### Purpose

Main page component for managing resources. Displays a table of all resources with inline editing capabilities and CRUD operations.

### Features

- **Resource Table:** Displays resources with name, type, and capacity columns
- **Inline Editing:** Click pencil icon to edit resource name and capacity inline
- **Create Resource:** Button to open `CreateResourceDialog`
- **Delete Resource:** Trash icon with confirmation
- **Empty State:** Shows message when no resources exist
- **Navigation:** Back arrow to return to Gantt chart

### Component Structure

```typescript
export function ResourcesPage()
```

**State:**
- `showCreateDialog: boolean` - Controls CreateResourceDialog visibility
- `editingId: string | null` - ID of resource being edited
- `editName: string` - Current edit value for name
- `editCapacity: number` - Current edit value for capacity

**Hooks Used:**
- `useResources()` - Fetch resources list
- `useCreateResource()` - Create new resource
- `useDeleteResource()` - Delete resource
- `useUpdateResource()` - Update resource

### Handlers

```typescript
handleCreate(data: { name: string; type: string; capacityHoursPerDay: number })
```
Creates a new resource via mutation.

```typescript
handleDelete(id: string)
```
Deletes a resource via mutation.

```typescript
startEdit(resource: Resource)
```
Initiates inline editing mode for a resource.

```typescript
saveEdit(id: string)
```
Saves inline edits via update mutation.

```typescript
cancelEdit()
```
Cancels inline editing mode without saving.

### UI Structure

```
ResourcesPage
├── Header
│   ├── Back Arrow (Link to "/")
│   ├── Title: "Ресурсы"
│   └── "Добавить ресурс" Button
├── Empty State (if no resources)
│   ├── Users Icon
│   ├── Message: "Ресурсы ещё не созданы"
│   └── "Создать первый ресурс" Button
└── Resource Table (if resources exist)
    ├── Header Row
    │   ├── Имя
    │   ├── Тип
    │   ├── Часы/день
    │   └── Actions (empty)
    └── Resource Rows
        ├── Name (editable inline)
        ├── Type Badge (Человек/Роль)
        ├── Capacity (editable inline)
        └── Action Buttons (Edit/Delete or Save/Cancel)
```

### Dependencies

**Imports:**
- React hooks: `useState`
- Icons: `Plus`, `Trash2`, `Pencil`, `User`, `Users`, `ArrowLeft` from `lucide-react`
- `Link` from `next/link`
- Hooks: `useResources`, `useCreateResource`, `useDeleteResource`, `useUpdateResource`
- Types: `Resource` from `@/types/schemas/resource.schema`
- Component: `CreateResourceDialog`

**Imported by:**
- App router page (likely `src/app/resources/page.tsx`)

### Usage Example

```tsx
// In app router page
import { ResourcesPage } from "@/components/Resources/ResourcesPage"

export default function ResourcesRoute() {
    return <ResourcesPage />
}
```

---

## CreateResourceDialog.tsx

### Purpose

Dialog component for creating new resources. Provides form fields for name, type, and capacity.

### Props

```typescript
interface CreateResourceDialogProps {
    open: boolean
    onClose: () => void
    onSubmit: (data: { name: string; type: string; capacityHoursPerDay: number }) => void
}
```

### Features

- **Name Input:** Text field for resource name
- **Type Selection:** Radio buttons or dropdown for PERSON/ROLE
- **Capacity Input:** Number field for hours per day (default: 8)
- **Validation:** Ensures required fields are filled
- **Cancel/Create Actions:** Buttons to close or submit

### Dependencies

**Imports:**
- React hooks (likely `useState`, `useEffect`)
- PrimeReact Dialog component (or similar)
- Icons from `lucide-react`

**Imported by:**
- `ResourcesPage.tsx`

---

## Resource Types

The application supports two resource types:

| Type | Description | Example |
|------|-------------|---------|
| **PERSON** | Individual team member | "John Doe" |
| **ROLE** | Role that can be filled by anyone | "Developer", "Designer" |

---

## Related Files

- [ResourceLoad Components](./components-resource-load.md)
- [Assignments Components](./components-assignments.md)
- [Hooks](./hooks.md#useResources)
- [Services](./services.md#resourcesapi)
- [Types](./types.md#resource-schema)
