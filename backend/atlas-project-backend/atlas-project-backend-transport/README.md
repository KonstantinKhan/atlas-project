# Backend Transport Module

**Path:** `/backend/atlas-project-backend/atlas-project-backend-transport`  
**Last Updated:** 2026-03-09

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
│   └── ProjectTaskStatus.kt
├── plan/
│   ├── GantPlanDto.kt
│   └── ProjectPlanDto.kt
├── timelineCalendar/
│   └── TimelineCalendarDto.kt
├── simple/
│   ├── ActualCalendarDuration.kt
│   ├── PlannedCalendarDuration.kt
│   └── ProjectTaskDescription.kt
└── cpm/
    └── CriticalPathDto.kt

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
