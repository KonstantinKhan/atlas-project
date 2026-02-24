# Interfaces Documentation

## Overview
This directory contains TypeScript interface specifications for the Atlas Project Web App.

## Interfaces

| Interface | Description | File |
|-----------|-------------|------|
| [`WorkCalendar`](./work-calendar.md) | Working calendar configuration with weekends, holidays, and exceptions | `src/types/interfaces/work-calendar.interface.ts` |

## Interface Template

When adding new interface documentation, follow this structure:

```markdown
# InterfaceName Interface

## Metadata
- **Kind:** TypeScript Interface
- **Name:** `InterfaceName`
- **Location:** `src/types/interfaces/interface-name.interface.ts`

## Purpose
Brief description of what the interface is for.

## Fields

### `fieldName`
- **Type:** `type`
- **Required:** Yes/No
- **Default/Format:** (if applicable)
- **Description:** What this field does

## Example
```typescript
const example: InterfaceName = { ... }
```

## Related
- Related files, hooks, services
```

## Related Documentation
- [Main Documentation Index](../index.md)
- [Project README](../../README.md)
