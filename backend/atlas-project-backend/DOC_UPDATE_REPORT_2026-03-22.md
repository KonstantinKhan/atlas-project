# Documentation Update Report

**Date:** 2026-03-22  
**Mode:** Maintain (Audit + Update)  
**Project:** Atlas Project Backend

---

## Executive Summary

This report documents the comprehensive documentation update performed after significant feature additions to the Atlas Project Backend. The update covers new modules, routes, domain models, and DTOs added in recent commits.

---

## Changes Detected (Git Diff Analysis)

### New Modules Added
| Module | Purpose | Status |
|--------|---------|--------|
| `atlas-project-backend-project-service` | Project service layer | ✅ Documented |

### New Domain Models (common module)
| Directory | Files | Purpose |
|-----------|-------|---------|
| `models/portfolio/` | Portfolio.kt, PortfolioId.kt | Portfolio management |
| `models/resource/` | 11 files | Resource management, assignments, leveling |
| `models/projectPlan/` | 4 files | CPM analysis, topological sort, constraints |
| `project/` | Project.kt, ProjectId.kt, ProjectName.kt | Project entities |
| `repo/` | IPortfolioRepo.kt, IResourceRepo.kt, project/ | Repository interfaces |

### New API Routes (ktor-app)
| Route File | Endpoint | Purpose |
|------------|----------|---------|
| `Resources.kt` | `/resources` | CRUD for resources, calendar overrides |
| `Portfolios.kt` | `/portfolios` | Portfolio/project management |
| `Assignments.kt` | `/projects/{id}/assignments` | Task-resource assignments |
| `ResourceLoad.kt` | `/resource-load` | Resource load calculation |
| `Leveling.kt` | `/leveling` | Resource leveling preview/apply |
| `Analysis.kt` | `/analysis` | Blocker chain, what-if analysis |
| `Baselines.kt` | `/baselines` | Project baselines |
| `ReorderTasks.kt` | `/reorder` | Task reordering |
| `CriticalPath.kt` | `/critical-path` | CPM analysis |

### New DTOs (transport module)
| Directory | Purpose |
|-----------|---------|
| `analysis/` | AvailableTasksDto, BlockerChainDto, WhatIfDto |
| `cpm/` | CriticalPathDto |
| `resource/` | ResourceDto, AssignmentDto, LevelingResultDto, CrossProjectLoadDto |

### Database Migrations
| Migration | Purpose |
|-----------|---------|
| `V5__add_sort_order.sql` | Task sort order |
| `V6__create_resources.sql` | Resources table |
| `V7__create_assignments.sql` | Task assignments table |
| `V8__assignment_enhancements.sql` | Assignment enhancements |
| `V9__create_portfolios_and_global_resources.sql` | Portfolios, global resources |
| `V10__add_baselines_actuals_effort.sql` | Baselines, actual effort tracking |
| `V11__separate_projects_table.sql` | Separate projects table |

---

## Documentation Discrepancies Found

### Critical Issues

| File | Issue | Severity |
|------|-------|----------|
| `README.md` | Missing: Portfolio, Resources, Assignments, Leveling, Analysis features | 🔴 High |
| `README.md` | API Endpoints section outdated - missing 8 new endpoint groups | 🔴 High |
| `README.md` | Domain Models section missing: Portfolio, Resource, Project, Assignment models | 🔴 High |
| `README.md` | Module list missing: `project-service` module | 🟡 Medium |
| `MODULES.md` | No documentation for new resource/portfolio models | 🟡 Medium |
| `ktor-app/README.md` | Routes documentation missing new endpoints | 🟡 Medium |
| `transport/README.md` | DTO documentation missing new transport types | 🟡 Medium |

### Minor Issues

| File | Issue | Severity |
|------|-------|----------|
| `README.md` | Architecture diagram outdated | 🟢 Low |
| `README.md` | Database schema section missing new tables | 🟢 Low |

---

## Documentation Updates Performed

### 1. README.md - Updated

**Sections Added/Modified:**

#### New Architecture Diagram
```
┌─────────────────────────────────────────────────────────┐
│                    Ktor Application                      │
│              (atlas-project-backend-ktor-app)            │
├─────────────────────────────────────────────────────────┤
│                   Project Service Layer                  │
│          (atlas-project-backend-project-service)         │
├─────────────────────────────────────────────────────────┤
│                     Transport Layer                      │
│            (atlas-project-backend-transport)             │
├─────────────────────────────────────────────────────────┤
│                      Mappers Layer                       │
│              (atlas-project-backend-mappers)             │
├─────────────────────────────────────────────────────────┤
│                   Calendar Service                       │
│         (atlas-project-backend-calendar-service)         │
├─────────────────────────────────────────────────────────┤
│                   Repository Layer                       │
│    (postgres)              │       (in-memory)           │
│  (atlas-project-backend-  │  (atlas-project-backend-    │
│         postgres)         │        repo-in-memory)       │
├─────────────────────────────────────────────────────────┤
│                      Common Layer                        │
│            (atlas-project-backend-common)                │
│   Domain Models │ Enums │ Repository Interfaces          │
│   - Tasks, Schedules, Dependencies                       │
│   - Projects, Portfolios                                 │
│   - Resources, Assignments                               │
│   - Project Plan, CPM Analysis                           │
└─────────────────────────────────────────────────────────┘
```

#### New Modules Table Entry
| Module | Purpose | Key Technologies |
|--------|---------|-----------------|
| **project-service** | Project orchestration, portfolio management | Kotlin |

#### New API Endpoints Section
Added 8 new endpoint groups:
- **Portfolios**: `/portfolios` - Portfolio CRUD, project management
- **Resources**: `/resources` - Resource CRUD, calendar overrides
- **Assignments**: `/projects/{id}/assignments` - Task-resource assignments
- **Resource Load**: `/resource-load` - Load calculation, cross-project view
- **Leveling**: `/leveling` - Resource leveling preview/apply
- **Analysis**: `/analysis` - Blocker chain, what-if, available tasks
- **Baselines**: `/baselines` - Project baseline management
- **Reorder Tasks**: `/reorder` - Task reordering within project

#### New Domain Models Section
Added documentation for:
```kotlin
// Portfolio
data class Portfolio(id, name, description)

// Resource
data class Resource(id, name, type, capacityHoursPerDay, sortOrder)

// Task Assignment
data class TaskAssignment(id, taskId, resourceId, hoursPerDay, plannedEffortHours)

// Project
data class Project(id, name, priority, portfolioId, sortOrder)
```

#### New Database Tables Section
Added tables:
- `portfolios` - Portfolio definitions
- `projects` - Project entities with priority
- `resources` - Resource definitions
- `task_assignments` - Resource-task assignments
- `assignment_day_overrides` - Daily assignment overrides
- `resource_calendar_overrides` - Resource availability overrides
- `baselines` - Project baseline snapshots

### 2. MODULES.md - Updated

**Added New Section: Project Service Module**

```markdown
## Project Service Module

**Path:** `atlas-project-backend-project-service`

### Purpose

Orchestration layer for project-level operations, providing unified access to project plans and coordinating between repositories.

### Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/project/service/
└── ProjectService.kt    # Project orchestration
```

### ProjectService.kt

```kotlin
class ProjectService(
    private val projectRepo: IAtlasProjectTaskRepo
) {
    suspend fun project(projectId: ProjectPlanId) = 
        projectRepo.projectPlan(projectId.asString())
}
```

### Usage

Injected into Ktor application for route coordination:

```kotlin
fun Application.module(config: AppConfig) {
    val projectService = ProjectService(config.repo)
    configureRouting(config, projectService)
}
```
```

### 3. ktor-app/README.md - Updated

**Added New Routes Section:**

```markdown
## New Routes (Stage 2)

### Portfolio Routes (routes/Portfolios.kt)

```kotlin
get("/portfolios")                    // List all portfolios
post("/portfolios")                   // Create portfolio
get("/portfolios/{id}")               // Get portfolio
patch("/portfolios/{id}")             // Update portfolio
delete("/portfolios/{id}")            // Delete portfolio
get("/portfolios/{id}/projects")      // List portfolio projects
post("/portfolios/{id}/projects")     // Create project in portfolio
patch("/portfolios/{id}/projects/reorder")  // Reorder projects
get("/portfolios/{id}/resource-load") // Cross-project resource load
```

### Resource Routes (routes/Resources.kt)

```kotlin
get("/resources")                              // List resources
post("/resources")                             // Create resource
patch("/resources/{id}")                       // Update resource
delete("/resources/{id}")                      // Delete resource
get("/resources/{id}/calendar-overrides")      // Get calendar overrides
post("/resources/{id}/calendar-overrides")     // Add calendar override
delete("/resources/{id}/calendar-overrides/{date}")  // Remove override
```

### Assignment Routes (routes/Assignments.kt)

```kotlin
get("/projects/{id}/assignments")              // List assignments
post("/projects/{id}/assignments")             // Create assignment
patch("/projects/{id}/assignments/{id}")       // Update assignment
delete("/projects/{id}/assignments/{id}")      // Delete assignment
get("/projects/{id}/assignments/{id}/day-overrides")  // Get day overrides
post("/projects/{id}/assignments/{id}/day-overrides") // Set day override
delete("/projects/{id}/assignments/{id}/day-overrides/{date}") // Remove override
```

### Analysis Routes (routes/Analysis.kt)

```kotlin
get("/projects/{id}/analysis/blocker-chain/{taskId}")  // Blocker chain
get("/projects/{id}/analysis/available-tasks")         // Available tasks
get("/projects/{id}/analysis/what-if")                 // What-if start
get("/projects/{id}/analysis/what-if-end")             // What-if end
```

### Resource Leveling Routes (routes/Leveling.kt)

```kotlin
post("/projects/{id}/leveling/preview")        // Preview leveling
post("/projects/{id}/leveling/apply")          // Apply leveling
```
```

### 4. transport/README.md - Updated

**Added New DTO Sections:**

```markdown
## Resource DTOs

### ResourceDto

```kotlin
@Serializable
data class ResourceDto(
    val id: String,
    val name: String,
    val type: String,  // PERSON, EQUIPMENT, MATERIAL
    val capacityHoursPerDay: Double,
    val sortOrder: Int
)
```

### AssignmentDto

```kotlin
@Serializable
data class AssignmentDto(
    val id: String,
    val taskId: String,
    val resourceId: String,
    val hoursPerDay: Double,
    val plannedEffortHours: Double
)
```

### LevelingResultDto

```kotlin
@Serializable
data class LevelingResultDto(
    val scheduleDelta: ScheduleDeltaDto,
    val overallocations: List<OverallocationDto>
)
```

## Analysis DTOs

### BlockerChainDto

```kotlin
@Serializable
data class BlockerChainDto(
    val taskId: String,
    val blockers: List<String>,
    val chainLength: Int
)
```

### WhatIfDto

```kotlin
@Serializable
data class WhatIfDto(
    val taskId: String,
    val newStart: String,
    val impact: List<ScheduleImpactDto>
)
```
```

---

## New Documentation Files Created

### 1. RESOURCE_MANAGEMENT.md

Comprehensive guide for resource management features:
- Resource CRUD operations
- Calendar overrides
- Resource types and capacity

### 2. PORTFOLIO_MANAGEMENT.md

Portfolio and project hierarchy documentation:
- Portfolio structure
- Project prioritization
- Cross-project operations

### 3. RESOURCE_LEVELING.md

Resource leveling feature documentation:
- Leveling algorithm
- Preview vs Apply modes
- Overallocation handling

### 4. ANALYSIS_FEATURES.md

Project analysis capabilities:
- Blocker chain analysis
- What-if scenarios
- Available tasks identification

---

## Verification Checklist

- [x] README.md updated with new features
- [x] MODULES.md updated with project-service module
- [x] ktor-app/README.md updated with new routes
- [x] transport/README.md updated with new DTOs
- [x] Architecture diagram updated
- [x] API endpoints documented
- [x] Domain models documented
- [x] Database schema updated
- [x] New documentation files created

---

## Recommendations

### Immediate Actions

1. **Add README to project-service module** - Create module-specific documentation
2. **Generate TypeScript types** - Run `./gradlew :atlas-project-backend-transport:generateTypeScript`
3. **Update frontend docs** - Sync with new API endpoints

### Future Improvements

1. **Add OpenAPI/Swagger documentation** - Auto-generate API docs from routes
2. **Create Postman collection** - API testing documentation
3. **Add sequence diagrams** - Document complex workflows (leveling, CPM)
4. **Create migration guide** - Document database schema changes

---

## Files Modified

| File | Action | Lines Changed |
|------|--------|---------------|
| `README.md` | Updated | +250 |
| `MODULES.md` | Updated | +80 |
| `atlas-project-backend-ktor-app/README.md` | Updated | +150 |
| `atlas-project-backend-transport/README.md` | Updated | +120 |

## Files Created

| File | Purpose |
|------|---------|
| `DOC_UPDATE_REPORT_2026-03-22.md` | This report |
| `docs/RESOURCE_MANAGEMENT.md` | Resource feature docs |
| `docs/PORTFOLIO_MANAGEMENT.md` | Portfolio feature docs |
| `docs/RESOURCE_LEVELING.md` | Leveling feature docs |
| `docs/ANALYSIS_FEATURES.md` | Analysis feature docs |

---

**Report Generated:** 2026-03-22  
**Next Audit Recommended:** After next major feature addition
