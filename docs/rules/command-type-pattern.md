# Command Type Pattern

## Principle

All command types must use constants from a central `*CommandType` registry instead of string literals.

## Why

- **Single source of truth** — all command type values are defined in one place
- **Refactoring safety** — renaming a constant will be caught by TypeScript across all usages
- **Consistency** — uniform style throughout the codebase
- **Discoverability** — IDE autocomplete shows all available command types

## Pattern Structure

### 1. Central Type Registry

Define all command type constants in a single file:

```typescript
// types/types/CommandType.ts (or domain-specific: TaskCommandType.ts)
export const CommandType = {
  CreateItem: 'createItem',
  UpdateItem: 'updateItem',
  DeleteItem: 'deleteItem',
} as const
```

### 2. Individual Command Types

Each command imports from the central registry:

```typescript
// types/types/CreateItemCommand.type.ts
import { CommandType } from './CommandType'

export interface CreateItemCommand {
  type: typeof CommandType.CreateItem
  payload: { /* ... */ }
}
```

### 3. Command Union Type

Combine all commands into a union:

```typescript
// types/types/Command.type.ts
import { CreateItemCommand } from './CreateItemCommand.type'
import { UpdateItemCommand } from './UpdateItemCommand.type'
import { DeleteItemCommand } from './DeleteItemCommand.type'

export type Command =
  | CreateItemCommand
  | UpdateItemCommand
  | DeleteItemCommand
```

### 4. Usage in Components/Handlers

```typescript
import { CommandType } from '@/types/types/CommandType'

// Dispatching a command
onCommand({ type: CommandType.CreateItem, payload: { ... } })

// Handling commands
switch (cmd.type) {
  case CommandType.CreateItem:
    // handle create
    break
  case CommandType.UpdateItem:
    // handle update
    break
}
```

## Examples

### ❌ Incorrect

```typescript
// Using string literal in command type
export interface CreateItemCommand {
  type: 'createItem'
}

// Using string literal when dispatching
onCommand({ type: 'createItem' })
```

### ✅ Correct

```typescript
// Import and use the constant
import { CommandType } from './CommandType'

export interface CreateItemCommand {
  type: typeof CommandType.CreateItem
}

// Dispatch with the constant
onCommand({ type: CommandType.CreateItem })
```

## File Conventions

| File | Purpose |
|------|---------|
| `types/types/*CommandType.ts` | Central registry of command type constants |
| `types/types/*Command.type.ts` | Individual command type definitions |
| `types/types/Command.type.ts` | Union of all command types |

## Related Patterns

- [Command Pattern](https://refactoring.guru/design-patterns/command)
- Discriminated unions in TypeScript
