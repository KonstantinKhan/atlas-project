# Backend Transport Module

**Path:** `/backend/atlas-project-backend/atlas-project-backend-transport`
**Last Updated:** 2026-03-22

## Overview

The transport module contains Data Transfer Objects (DTOs) for API communication. This is a **Kotlin Multiplatform** module that provides serializable types for HTTP requests/responses and generates TypeScript types for the frontend.

## Purpose

- **DTOs**: Define API request/response shapes
- **Serialization**: Kotlinx Serialization for JSON (de)serialization
- **TypeScript Generation**: Auto-generate TypeScript types from Kotlin DTOs
- **API Contracts**: Single source of truth for API schema

## Structure

```
src/commonMain/kotlin/com/khan366kos/atlas/project/backend/transport/
├── GanttTaskDto.kt                    # Task DTO for Gantt
├── GanttDependencyDto.kt              # Dependency DTO
├── TaskDto.kt                         # Basic task DTO
├── ScheduledTaskDto.kt                # Scheduled task DTO
├── ScheduleDeltaDto.kt                # Schedule change DTO
├── CreateProjectTaskRequest.kt        # Create task request
├── UpdateProjectTaskRequest.kt        # Update task request
├── WorkCalendar.kt                    # Work calendar DTO
├── commands/
│   ├── AssignScheduleCommandDto.kt
│   ├── ChangeDependencyTypeCommandDto.kt
│   ├── ChangeTaskEndDateCommandDto.kt
│   ├── ChangeTaskStartDateCommandDto.kt
│   ├── CreateDependencyCommandDto.kt
│   ├── CreateTaskInPoolCommandDto.kt
│   └── PlanFromEndCommandDto.kt
├── enums/
│   ├── DependencyTypeDto.kt
│   ├── ProjectTaskStatus.kt
│   ├── ProjectPriorityDto.kt
│   └── ResourceTypeDto.kt
├── plan/
│   ├── GantPlanDto.kt
│   └── ProjectPlanDto.kt
├── timelineCalendar/
│   └── TimelineCalendarDto.kt
├── simple/
│   ├── ActualCalendarDuration.kt
│   ├── PlannedCalendarDuration.kt
│   └── ProjectTaskDescription.kt
├── cpm/
│   └── CriticalPathDto.kt
├── analysis/                          # New (Stage 2)
│   ├── AvailableTasksDto.kt
│   ├── BlockerChainDto.kt
│   └── WhatIfDto.kt
├── resource/                          # New (Stage 2)
│   ├── ResourceDto.kt
│   ├── ResourceListDto.kt
│   ├── CreateResourceCommandDto.kt
│   ├── UpdateResourceCommandDto.kt
│   ├── ResourceCalendarOverrideDto.kt
│   ├── AssignmentDto.kt
│   ├── AssignmentListDto.kt
│   ├── CreateAssignmentCommandDto.kt
│   ├── UpdateAssignmentCommandDto.kt
│   ├── AssignmentDayOverrideDto.kt
│   ├── LevelingResultDto.kt
│   └── CrossProjectLoadDto.kt
└── portfolio/                         # New (Stage 2)
    ├── PortfolioDto.kt
    ├── PortfolioListDto.kt
    ├── CreatePortfolioRequest.kt
    ├── ProjectSummaryDto.kt
    └── CreateProjectRequest.kt

src/jvmMain/kotlin/
└── com/khan366kos/atlas/project/backend/transport/
    └── GenerateTypeScript.kt          # TypeScript generator
```

## Key DTOs

### GanttTaskDto

```kotlin
@Serializable
data class GanttTaskDto(
    val id: String,
    val title: String,
    val description: String,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val status: ProjectTaskStatus
)
```

**Usage**: Main task representation for Gantt chart.

### GanttDependencyDto

```kotlin
@Serializable
data class GanttDependencyDto(
    val fromTaskId: String,
    val toTaskId: String,
    val type: DependencyTypeDto
)
```

**Usage**: Represents task dependency in the UI.

### GanttProjectPlanDto

```kotlin
@Serializable
data class GanttProjectPlanDto(
    val projectId: String,
    val tasks: List<GanttTaskDto>,
    val dependencies: List<GanttDependencyDto>
)
```

**Usage**: Complete project plan response.

### ScheduleDeltaDto

```kotlin
@Serializable
data class ScheduleDeltaDto(
    val updatedSchedules: List<UpdatedScheduleDto>
)

@Serializable
data class UpdatedScheduleDto(
    val taskId: String,
    val start: String,
    val end: String
)
```

**Usage**: Response from schedule change operations.

### CriticalPathDto

```kotlin
@Serializable
data class CriticalPathDto(
    val criticalTaskIds: List<String>,
    val tasks: List<CpmTaskDto>
)

@Serializable
data class CpmTaskDto(
    val taskId: String,
    val slack: Int
)
```

**Usage**: Critical path analysis response.

### TimelineCalendarDto

```kotlin
@Serializable
data class TimelineCalendarDto(
    val id: String,
    val name: String,
    val workingWeek: List<DayOfWeekDto>,
    val holidays: List<HolidayDto>,
    val workingWeekends: List<String>
)

@Serializable
data class HolidayDto(
    val date: String,
    val name: String
)
```

**Usage**: Calendar configuration.

## Command DTOs

### CreateTaskInPoolCommandDto

```kotlin
@Serializable
data class CreateTaskInPoolCommandDto(
    val title: String
)
```

**Endpoint**: `POST /project-tasks/create-in-pool`

### AssignScheduleCommandDto

```kotlin
@Serializable
data class AssignScheduleCommandDto(
    val taskId: String,
    val start: String,
    val duration: Int
)
```

**Endpoint**: `POST /project-tasks/:id/schedule`

### ChangeTaskStartDateCommandDto

```kotlin
@Serializable
data class ChangeTaskStartDateCommandDto(
    val planId: String,
    val taskId: String,
    val newPlannedStart: String
)
```

**Endpoint**: `POST /change-start`

### ChangeDependencyTypeCommandDto

```kotlin
@Serializable
data class ChangeDependencyTypeCommandDto(
    val fromTaskId: String,
    val toTaskId: String,
    val newType: String
)
```

**Endpoint**: `PATCH /dependencies`

### CreateDependencyCommandDto

```kotlin
@Serializable
data class CreateDependencyCommandDto(
    val planId: String,
    val fromTaskId: String,
    val toTaskId: String,
    val type: String = "FS"
)
```

**Endpoint**: `POST /dependencies`

## Enum DTOs

### DependencyTypeDto

```kotlin
@Serializable
enum class DependencyTypeDto {
    FS,  // Finish-to-Start
    SS,  // Start-to-Start
    FF,  // Finish-to-Finish
    SF   // Start-to-Finish
}
```

### ProjectTaskStatus

```kotlin
@Serializable
enum class ProjectTaskStatus {
    EMPTY,
    PLANNED,
    IN_PROGRESS,
    COMPLETED
}
```

### ProjectPriorityDto (New)

```kotlin
@Serializable
enum class ProjectPriorityDto {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

### ResourceTypeDto (New)

```kotlin
@Serializable
enum class ResourceTypeDto {
    PERSON,
    EQUIPMENT,
    MATERIAL
}
```

## New DTOs (Stage 2 - Resource & Portfolio Management)

### Resource DTOs

#### ResourceDto

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

**Endpoint**: `GET /resources`, `POST /resources`, `PATCH /resources/{id}`

#### ResourceListDto

```kotlin
@Serializable
data class ResourceListDto(
    val resources: List<ResourceDto>
)
```

**Endpoint**: `GET /resources`

#### CreateResourceCommandDto

```kotlin
@Serializable
data class CreateResourceCommandDto(
    val name: String,
    val type: String,
    val capacityHoursPerDay: Double
)
```

**Endpoint**: `POST /resources`

#### UpdateResourceCommandDto

```kotlin
@Serializable
data class UpdateResourceCommandDto(
    val name: String?,
    val type: String?,
    val capacityHoursPerDay: Double?
)
```

**Endpoint**: `PATCH /resources/{id}`

#### ResourceCalendarOverrideDto

```kotlin
@Serializable
data class ResourceCalendarOverrideDto(
    val date: String,
    val availableHours: Double
)
```

**Endpoint**: `POST /resources/{id}/calendar-overrides`

### Assignment DTOs

#### AssignmentDto

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

**Endpoint**: `GET /projects/{id}/assignments`, `POST /projects/{id}/assignments`

#### AssignmentListDto

```kotlin
@Serializable
data class TaskAssignmentListDto(
    val assignments: List<AssignmentDto>
)
```

**Endpoint**: `GET /projects/{id}/assignments`

#### CreateAssignmentCommandDto

```kotlin
@Serializable
data class CreateAssignmentCommandDto(
    val taskId: String,
    val resourceId: String,
    val hoursPerDay: Double,
    val plannedEffortHours: Double
)
```

**Endpoint**: `POST /projects/{id}/assignments`

#### UpdateAssignmentCommandDto

```kotlin
@Serializable
data class UpdateAssignmentCommandDto(
    val hoursPerDay: Double?,
    val plannedEffortHours: Double?
)
```

**Endpoint**: `PATCH /projects/{id}/assignments/{id}`

#### AssignmentDayOverrideDto

```kotlin
@Serializable
data class AssignmentDayOverrideDto(
    val date: String,
    val hours: Double
)
```

**Endpoint**: `POST /projects/{id}/assignments/{id}/day-overrides`

### Resource Leveling DTOs

#### LevelingResultDto

```kotlin
@Serializable
data class LevelingResultDto(
    val scheduleDelta: ScheduleDeltaDto,
    val overallocations: List<OverallocationDto>
)

@Serializable
data class OverallocationDto(
    val resourceId: String,
    val date: String,
    val allocatedHours: Double,
    val capacityHours: Double
)
```

**Endpoint**: `POST /projects/{id}/leveling/preview`, `POST /projects/{id}/leveling/apply`

#### CrossProjectLoadDto

```kotlin
@Serializable
data class CrossProjectLoadDto(
    val from: String,
    val to: String,
    val resourceLoads: List<ResourceLoadEntryDto>
)

@Serializable
data class ResourceLoadEntryDto(
    val resourceId: String,
    val resourceName: String,
    val dailyLoads: List<DailyLoadDto>
)

@Serializable
data class DailyLoadDto(
    val date: String,
    val allocatedHours: Double,
    val capacityHours: Double,
    val overallocated: Boolean
)
```

**Endpoint**: `GET /portfolios/{id}/resource-load`

### Portfolio DTOs

#### PortfolioDto

```kotlin
@Serializable
data class PortfolioDto(
    val id: String,
    val name: String,
    val description: String
)
```

**Endpoint**: `GET /portfolios`, `POST /portfolios`, `GET /portfolios/{id}`

#### PortfolioListDto

```kotlin
@Serializable
data class PortfolioListDto(
    val portfolios: List<PortfolioDto>
)
```

**Endpoint**: `GET /portfolios`

#### CreatePortfolioRequest

```kotlin
@Serializable
data class CreatePortfolioRequest(
    val name: String,
    val description: String = ""
)
```

**Endpoint**: `POST /portfolios`

#### ProjectSummaryDto

```kotlin
@Serializable
data class ProjectSummaryDto(
    val id: String,
    val name: String,
    val priority: ProjectPriorityDto,
    val taskCount: Int
)
```

**Endpoint**: `GET /portfolios/{id}/projects`

#### CreateProjectRequest

```kotlin
@Serializable
data class CreateProjectRequest(
    val name: String,
    val priority: ProjectPriorityDto = ProjectPriorityDto.MEDIUM
)
```

**Endpoint**: `POST /portfolios/{id}/projects`

### Analysis DTOs

#### BlockerChainDto

```kotlin
@Serializable
data class BlockerChainDto(
    val taskId: String,
    val blockers: List<String>,
    val chainLength: Int
)
```

**Endpoint**: `GET /projects/{id}/analysis/blocker-chain/{taskId}`

#### AvailableTasksDto

```kotlin
@Serializable
data class AvailableTasksDto(
    val availableTasks: List<AvailableTaskEntryDto>
)

@Serializable
data class AvailableTaskEntryDto(
    val taskId: String,
    val title: String,
    val canStart: Boolean
)
```

**Endpoint**: `GET /projects/{id}/analysis/available-tasks`

#### WhatIfDto

```kotlin
@Serializable
data class WhatIfDto(
    val taskId: String,
    val newStart: String,
    val impactedTasks: List<ImpactedTaskDto>
)

@Serializable
data class ImpactedTaskDto(
    val taskId: String,
    val originalStart: String,
    val newStart: String,
    val delta: Int
)
```

**Endpoint**: `GET /projects/{id}/analysis/what-if`

## TypeScript Generation

### Setup

The transport module includes a TypeScript generator:

```kotlin
// GenerateTypeScript.kt
@OptIn(KxsTsGenApi::class)
fun main(args: Array<String>) {
    val outputDir = args[0]
    generateTypeScript(
        types = listOf(
            GanttTaskDto::class,
            GanttDependencyDto::class,
            // ... more DTOs
        ),
        outputDirectory = outputDir
    )
}
```

### Running Generation

```bash
# From backend root
./gradlew :atlas-project-backend-transport:generateTypeScript
```

### Output Location

Generated types are placed in:
```
frontend/atlas-project-web-app/src/types/generated/
```

### Generated TypeScript Example

```typescript
// Generated from GanttTaskDto
export interface GanttTaskDto {
  id: string;
  title: string;
  description: string;
  start: string | null;
  end: string | null;
  status: ProjectTaskStatus;
}

// Generated from DependencyTypeDto
export type DependencyTypeDto = "FS" | "SS" | "FF" | "SF";
```

## Serialization Configuration

DTOs use Kotlinx Serialization:

```kotlin
@Serializable
data class ExampleDto(
    @SerialName("task_id")  // Custom JSON property name
    val taskId: String,
    val title: String
)
```

Configuration in `ktor-app` module:

```kotlin
// Serialization.kt
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}
```

## API Request/Response Examples

### Get Project Plan

**Request**:
```http
GET /project-plan
Accept: application/json
```

**Response**:
```json
{
  "projectId": "plan-1",
  "tasks": [
    {
      "id": "task-1",
      "title": "Design",
      "description": "System design",
      "start": "2026-03-15",
      "end": "2026-03-20",
      "status": "PLANNED"
    }
  ],
  "dependencies": [
    {
      "fromTaskId": "task-1",
      "toTaskId": "task-2",
      "type": "FS"
    }
  ]
}
```

### Create Task

**Request**:
```http
POST /project-tasks/create-in-pool
Content-Type: application/json

{
  "title": "New Task"
}
```

**Response**:
```json
{
  "id": "task-123",
  "title": "New Task",
  "description": "",
  "start": null,
  "end": null,
  "status": "EMPTY"
}
```

### Change Task Start Date

**Request**:
```http
POST /change-start
Content-Type: application/json

{
  "planId": "plan-1",
  "taskId": "task-1",
  "newPlannedStart": "2026-03-20"
}
```

**Response**:
```json
{
  "updatedSchedules": [
    {
      "taskId": "task-1",
      "start": "2026-03-20",
      "end": "2026-03-25"
    },
    {
      "taskId": "task-2",
      "start": "2026-03-26",
      "end": "2026-03-30"
    }
  ]
}
```

## Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        jvmMain.dependencies {
            implementation(libs.kxs.ts.gen.core)
        }
    }
}

tasks.register<JavaExec>("generateTypeScript") {
    description = "Generate TypeScript interfaces"
    group = "codegen"
    
    val jvmCompilation = kotlin.jvm().compilations["main"]
    classpath = files(
        jvmCompilation.output.allOutputs,
        jvmCompilation.runtimeDependencyFiles
    )
    mainClass.set("com.khan366kos.atlas.project.backend.transport.GenerateTypeScriptKt")
    
    val outputDir = file("${project.rootDir.parentFile.parentFile}/frontend/atlas-project-web-app/src/types/generated")
    args(outputDir.absolutePath)
    
    dependsOn("compileKotlinJvm")
}
```

## Dependencies

- `kotlinx.serialization.json` - JSON serialization
- `kotlinx.datetime` - Date/time handling
- `kxs-ts-gen-core` - TypeScript generation library

## Related Modules

- **common**: Domain models mapped to DTOs
- **mappers**: Domain ↔ Transport mapping
- **ktor-app**: Uses DTOs for API endpoints
