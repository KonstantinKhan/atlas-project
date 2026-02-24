# QWEN.md - AI Assistant Guidelines

## Documentation-First Approach

**You are a docs-first AI assistant.** Always prioritize reading and following project documentation before making any changes or providing recommendations.

### Documentation Navigation Workflow

1. **Start with `@README.md`** — Get general project overview, technology stack, and setup instructions

2. **Check `@docs/index.md`** — Navigate the documentation structure to find relevant topics

3. **Read interface documentation from `@docs/interfaces/index.md`** — When working with TypeScript interfaces, types, or data structures

4. **Proceed to specific documentation** — Based on your task, read the relevant docs files to understand:
   - Interface definitions and field descriptions
   - Component behavior and props
   - API contracts and data flow
   - Business logic and rules

### Decision Making

After reading the documentation:
- **If documentation is clear** — Follow it strictly when implementing changes
- **If documentation is ambiguous** — Ask the user for clarification before proceeding
- **If documentation is missing** — Inform the user and suggest creating documentation for the missing topic

### Documentation Location

All project documentation is located in the `@docs/` directory:
- `@docs/index.md` — Main documentation index
- `@docs/interfaces/` — TypeScript interface documentation
- `@docs/interfaces/work-calendar.md` — WorkCalendar interface specification

### Example Workflow

```
Task: "Update the WorkCalendar interface"

1. Read @README.md → Understand project structure
2. Read @docs/index.md → Find interface documentation
3. Read @docs/interfaces/index.md → Locate WorkCalendar docs
4. Read @docs/interfaces/work-calendar.md → Understand fields and usage
5. Make informed decision about changes
```

## Project Context

- **Framework:** Next.js 16.1.6 (App Router)
- **Language:** TypeScript
- **State Management:** Zustand
- **Data Fetching:** TanStack Query (React Query)
- **Styling:** Tailwind CSS v4
- **UI Components:** PrimeReact, lucide-react
