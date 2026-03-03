# PROJECT_OVERVIEW

## Project Structure

- `/backend/` - Backend application modules
- `/frontend/` - Frontend web application
- `/docs/` - Project documentation
- `/postgres/` - PostgreSQL database configuration

## Backend Structure

Backend located in `/backend/atlas-project-backend/` with the following modules:

- `atlas-project-backend-common` - Shared domain models and common utilities
- `atlas-project-backend-transport` - DTOs and API request/response models
- `atlas-project-backend-mappers` - Mappers between transport DTOs and domain models
- `atlas-project-backend-ktor-app` - Ktor-based REST API application
- `atlas-project-backend-calendar-service` - Calendar business logic service
- `atlas-project-backend-repo-in-memory` - In-memory repository implementation
- `atlas-project-backend-postgres` - PostgreSQL database repository implementation

**Tech stack:** Kotlin 2.3, Ktor 3.4, Exposed 0.61, PostgreSQL, Flyway, Kotlinx Serialization, Kotlinx Coroutines, Logback

## Frontend Structure

Frontend located in `/frontend/atlas-project-web-app/` - Next.js React application:

- `src/app/` - Next.js App Router pages and layouts
- `src/components/` - Reusable UI components
  - [GanttChart](../docs/frontend/COMPONENTS.md) — Main project visualization
  - [TaskCard](../docs/frontend/COMPONENTS.md) — Task card display
- `src/services/` - API client services (project tasks, timeline calendar)
- `src/store/` - State management with Zustand
- `src/types/` - TypeScript types, interfaces, enums, and Zod schemas
- `src/hooks/` - Custom React hooks
- `src/providers/` - React context providers
- `src/utils/` - Utility functions
- `src/constants/` - Application constants
- `public/` - Static assets

**Tech stack:** Next.js 16, React 19, TypeScript, Tailwind CSS, Zustand, React Query, PrimeReact
