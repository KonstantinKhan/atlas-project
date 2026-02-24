# Atlas Project Web App

A Gantt chart-based project management web application built with Next.js 16, React 19, and TypeScript.

## Features

- **Gantt Chart Visualization** — Interactive timeline for project tasks
- **Task Management** — Create, edit, and track tasks with dependencies
- **Dependency Tracking** — Visual task dependencies with link creation/removal
- **Working Calendar** — Configurable weekends, holidays, and working exceptions
- **Multi-user Ready** — React Query + Zustand architecture for API-driven data
- **Dark Mode** — Full dark theme support via Tailwind CSS

## Tech Stack

| Category | Technology |
|----------|------------|
| Framework | Next.js 16.1.6 (App Router) |
| Language | TypeScript 5 |
| State Management | Zustand |
| Data Fetching | TanStack Query v5 |
| Styling | Tailwind CSS v4 |
| UI Components | PrimeReact, lucide-react |
| Drag & Drop | @dnd-kit/react |

## Getting Started

### Prerequisites
- Node.js 20+
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Start production server
npm start

# Run linter
npm run lint
```

### Environment Variables

Create a `.env.local` file in the root:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Project Structure

```
atlas-project-web-app/
├── src/
│   ├── app/                    # Next.js App Router
│   │   ├── layout.tsx          # Root layout with providers
│   │   ├── page.tsx            # Home page
│   │   └── globals.css         # Global styles
│   ├── components/
│   │   └── GanttChart/         # Gantt chart components
│   │       ├── GanttChart.tsx
│   │       ├── GanttCalendarGrid.tsx
│   │       ├── GanttCalendarHeader.tsx
│   │       ├── GanttTaskList.tsx
│   │       ├── GanttBar.tsx
│   │       └── GanttChart.styles.ts
│   ├── hooks/                  # Custom React hooks
│   │   └── useWorkCalendar.ts
│   ├── providers/              # Context providers
│   │   └── QueryProvider.tsx
│   ├── services/               # API service functions
│   │   └── workCalendarApi.ts
│   ├── store/                  # Zustand stores
│   │   └── workCalendarStore.ts
│   ├── types/                  # TypeScript types
│   │   ├── enums/
│   │   └── interfaces/
│   └── utils/                  # Utility functions
│       ├── ganttDateUtils.ts
│       └── ganttDependencyUtils.ts
├── docs/                       # Documentation
│   ├── index.md
│   └── interfaces/
├── public/                     # Static assets
├── QWEN.md                     # AI assistant guidelines
└── package.json
```

## Key Concepts

### Working Calendar
The application uses a configurable working calendar system that supports:
- Custom weekend days (default: Saturday, Sunday)
- Holiday dates (non-working regardless of day of week)
- Working weekend exceptions (rescheduled weekends)

See [WorkCalendar Interface](./docs/interfaces/work-calendar.md) for details.

### Data Architecture
- **Server State** — Managed by React Query (caching, refetch, invalidation)
- **UI State** — Managed by Zustand (settings, panel states, cached data access)
- **Optimistic Updates** — Supported for responsive UI during API calls

### Component Architecture
- **GanttChart** — Main container component
- **GanttCalendarHeader** — Month/day headers
- **GanttCalendarGrid** — Interactive grid with task bars
- **GanttTaskList** — Left panel with task details

## API Integration

The application expects the following API endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/work-calendar` | Get working calendar configuration |
| `PUT` | `/api/work-calendar` | Update working calendar configuration |

Configure the API base URL via `NEXT_PUBLIC_API_URL` environment variable.

## Documentation

- [Main Documentation](./docs/index.md)
- [Interface Specifications](./docs/interfaces/index.md)
- [AI Assistant Guidelines](./QWEN.md)

## Contributing

1. Follow the docs-first approach (see [QWEN.md](./QWEN.md))
2. Update documentation when adding new features
3. Run `npm run lint` before committing
4. Ensure build passes: `npm run build`

## License

Internal project — Atlas Project
