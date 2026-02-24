# Atlas Project Web App - Documentation

## Project Overview
Atlas Project Web App is a Gantt chart-based project management interface built with Next.js, React, and TypeScript.

## Documentation Structure

### Core Documentation
| Document | Description |
|----------|-------------|
| [README](../README.md) | Project setup, development, and deployment guide |
| [QWEN.md](../QWEN.md) | AI assistant guidelines and documentation-first workflow |

### Technical Documentation

#### Interfaces
TypeScript interface specifications and data structures.

- [Interfaces Index](./interfaces/index.md) — Complete list of interfaces
- [WorkCalendar](./interfaces/work-calendar.md) — Working calendar configuration

#### Components
React component documentation (coming soon).

#### Hooks
Custom React hooks documentation (coming soon).

#### Services
API service layer documentation (coming soon).

#### Store
Zustand store documentation (coming soon).

## Quick Links

### Key Files
```
src/
├── app/                    # Next.js App Router pages
├── components/             # React components
│   └── GanttChart/         # Gantt chart components
├── hooks/                  # Custom React hooks
├── services/               # API service functions
├── store/                  # Zustand stores
├── types/                  # TypeScript types and interfaces
└── utils/                  # Utility functions
```

### Technology Stack
- **Framework:** Next.js 16.1.6 (App Router)
- **Language:** TypeScript 5
- **State:** Zustand
- **Data Fetching:** TanStack Query v5
- **Styling:** Tailwind CSS v4
- **UI:** PrimeReact, lucide-react

## Contributing

When adding new features:
1. Update relevant interface documentation in `docs/interfaces/`
2. Document new components and hooks
3. Keep this index updated with new sections
