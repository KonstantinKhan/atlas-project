# Atlas Project Documentation Map

**Project:** Atlas Project - Gantt Chart Application  
**Version:** 0.0.1  
**Last Updated:** 2026-03-09

## Quick Navigation

| Module | Documentation | Description |
|--------|--------------|-------------|
| **Frontend** | [README](./frontend/atlas-project-web-app/README.md) | Next.js Gantt Chart Web Application |
| **Backend** | [README](./backend/atlas-project-backend/README.md) | Kotlin Multi-Module REST API |
| **Frontend Components** | [Components](./frontend/atlas-project-web-app/src/components/README.md) | React Component Documentation |
| **Frontend Hooks** | [Hooks](./frontend/atlas-project-web-app/src/hooks/README.md) | Custom React Hooks |
| **Frontend Services** | [Services](./frontend/atlas-project-web-app/src/services/README.md) | API Service Layer |
| **Frontend Types** | [Types](./frontend/atlas-project-web-app/src/types/README.md) | TypeScript Types & Schemas |
| **Backend Common** | [Common](./backend/atlas-project-backend/atlas-project-backend-common/README.md) | Domain Models & Business Logic |
| **Backend Transport** | [Transport](./backend/atlas-project-backend/atlas-project-backend-transport/README.md) | API DTOs & TypeScript Generation |
| **Backend Ktor App** | [Ktor App](./backend/atlas-project-backend/atlas-project-backend-ktor-app/README.md) | REST API Server |
| **Backend Modules** | [Supporting Modules](./backend/atlas-project-backend/MODULES.md) | Mappers, Repositories, Calendar Service |

## Project Overview

Atlas Project is a full-stack Gantt chart application for project planning and task management with:

- **Interactive Timeline**: Day/week view modes with drag-and-drop scheduling
- **Task Management**: Create, edit, delete, and schedule tasks
- **Dependency Tracking**: FS, SS, FF, SF dependency types with visual links
- **Critical Path Analysis**: Automatic CPM calculation and highlighting
- **Calendar Awareness**: Working days, weekends, and holiday support

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Next.js)                      │
│  React 19 │ TypeScript │ Tailwind CSS │ TanStack Query      │
├─────────────────────────────────────────────────────────────┤
│                         REST API                             │
│                    HTTP/JSON (Port 8080)                     │
├─────────────────────────────────────────────────────────────┤
│                      Backend (Kotlin)                        │
│  Ktor │ Exposed ORM │ PostgreSQL │ Kotlinx Serialization    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Multi-Module Architecture                           │   │
│  │  ├── ktor-app (REST API)                             │   │
│  │  ├── transport (DTOs + TS Generation)                │   │
│  │  ├── mappers (Domain ↔ Transport)                    │   │
│  │  ├── calendar-service (Working Calendar)             │   │
│  │  ├── postgres (Production Repository)                │   │
│  │  ├── repo-in-memory (Test Repository)                │   │
│  │  └── common (Domain Models + CPM)                    │   │
│  └──────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                       PostgreSQL                             │
│  project_tasks │ task_schedules │ task_dependencies │ ...   │
└─────────────────────────────────────────────────────────────┘
```

## For Agents

### Starting Points

1. **New to the project?** → Read this file, then module READMEs
2. **Frontend changes?** → [Frontend README](./frontend/atlas-project-web-app/README.md)
3. **Backend changes?** → [Backend README](./backend/atlas-project-backend/README.md)
4. **API changes?** → [Transport Module](./backend/atlas-project-backend/atlas-project-backend-transport/README.md) + [Ktor App](./backend/atlas-project-backend/atlas-project-backend-ktor-app/README.md)

### Key Conventions

#### Frontend
- **Components**: `src/components/` with feature subdirectories
- **Hooks**: `src/hooks/` using TanStack Query
- **Services**: `src/services/` for API calls
- **Types**: `src/types/` with Zod schemas
- **State**: Zustand for UI state, React Query for server state

#### Backend
- **Domain Models**: `common` module (pure Kotlin)
- **DTOs**: `transport` module (serializable)
- **Mapping**: `mappers` module (bidirectional)
- **Repositories**: `postgres` (production), `repo-in-memory` (testing)
- **Business Logic**: `common` module (CPM, topological sort)

### Development Workflow

```bash
# Frontend Development
cd frontend/atlas-project-web-app
npm install
npm run dev

# Backend Development
cd backend/atlas-project-backend
./gradlew build
./gradlew :atlas-project-backend-ktor-app:run

# Generate TypeScript types from Kotlin DTOs
./gradlew :atlas-project-backend-transport:generateTypeScript

# Run tests
./gradlew test          # Backend
npm test               # Frontend
```

## API Quick Reference

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/project-tasks` | Get all tasks |
| POST | `/project-tasks/create-in-pool` | Create task |
| PATCH | `/project-tasks/:id` | Update task |
| DELETE | `/project-tasks/:id` | Delete task |
| GET | `/project-plan` | Get full project plan |
| POST | `/change-start` | Change task start |
| POST | `/change-end` | Change task end |
| POST | `/resize-from-start` | Resize from start |
| POST | `/plan-from-end` | Plan from end |
| POST | `/dependencies` | Create dependency |
| DELETE | `/dependencies` | Remove dependency |
| PATCH | `/dependencies` | Change dependency type |
| GET | `/critical-path` | Get CPM analysis |
| GET | `/timeline-calendar` | Get calendar |
| PUT | `/timeline-calendar` | Update calendar |

### Request/Response Examples

**Create Task**:
```json
POST /project-tasks/create-in-pool
{"title": "New Task"}

→ {"id": "task-123", "title": "New Task", "status": "EMPTY"}
```

**Get Project Plan**:
```json
GET /project-plan

→ {
  "projectId": "plan-1",
  "tasks": [...],
  "dependencies": [...]
}
```

## Technology Stack

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 16.1.6 | React Framework |
| React | 19.2.3 | UI Library |
| TypeScript | 5.x | Type Safety |
| Tailwind CSS | 4.x | Styling |
| Zustand | 5.0.11 | State Management |
| TanStack Query | 5.90.21 | Data Fetching |
| @dnd-kit/react | 0.3.2 | Drag & Drop |
| Zod | 4.3.6 | Validation |

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.x | Language |
| Ktor | 3.x | Web Framework |
| Exposed | 0.x | ORM |
| PostgreSQL | 42.x | Database |
| Flyway | 10.x | Migrations |
| Kotlinx Serialization | 1.x | JSON |
| Kotlinx DateTime | 0.x | Date/Time |

## File Structure

```
atlas-project/
├── frontend/
│   └── atlas-project-web-app/
│       ├── src/
│       │   ├── app/           # Next.js App Router
│       │   ├── components/    # React Components
│       │   ├── hooks/         # Custom Hooks
│       │   ├── services/      # API Services
│       │   ├── store/         # Zustand Store
│       │   ├── types/         # TypeScript Types
│       │   ├── utils/         # Utilities
│       │   └── providers/     # Context Providers
│       ├── package.json
│       └── tsconfig.json
├── backend/
│   └── atlas-project-backend/
│       ├── atlas-project-backend-ktor-app/
│       ├── atlas-project-backend-transport/
│       ├── atlas-project-backend-mappers/
│       ├── atlas-project-backend-calendar-service/
│       ├── atlas-project-backend-postgres/
│       ├── atlas-project-backend-repo-in-memory/
│       ├── atlas-project-backend-common/
│       ├── build.gradle.kts
│       └── settings.gradle.kts
├── docs/                    # Project Documentation
├── postgres/                # Database Configuration
└── QWEN.md                 # Agent Instructions
```

## Related Documentation

- [QWEN.md](./QWEN.md) - Agent orchestration instructions
- [CLAUDE.md](./CLAUDE.md) - Additional project context

## Maintenance

### Documentation Updates

When making code changes, update corresponding documentation:

| Code Change | Documentation to Update |
|-------------|------------------------|
| New API endpoint | Backend README + Transport README |
| New component | Components README |
| New hook | Hooks README |
| Domain model change | Common README + Transport README |
| Database schema change | Ktor App README (Tables section) |

### TypeScript Type Sync

After changing backend DTOs:

```bash
cd backend/atlas-project-backend
./gradlew :atlas-project-backend-transport:generateTypeScript
```

## Contact & Support

For questions about this documentation, refer to the module-specific README files or check the code comments.
