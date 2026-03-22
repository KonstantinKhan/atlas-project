# Common Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-common/`
**Module:** [Backend Index](../INDEX.md)
**Last Updated:** 2026-03-22

## Purpose

The Common module contains domain models and repository interfaces. It defines the core business entities and contracts for data access, independent of any infrastructure concerns.

## Directory Structure

```
src/commonMain/kotlin/com/khan366kos/atlas/project/backend/common/
├── models/
│   ├── projectPlan/
│   │   ├── ProjectPlan.kt
│   │   └── ProjectPlanId.kt
│   ├── task/
│   │   ├── enums/
│   │   ├── simple/
│   │   └── ProjectTask.kt
│   ├── taskSchedule/
│   │   ├── TaskSchedule.kt
│   │   └── TaskScheduleId.kt
│   ├── timelineCalendar/
│   ├── simple/
│   ├── ProjectDate.kt
│   └── TaskDependency.kt
├── project/
│   └── Project.kt
├── repo/
│   ├── IAtlasProjectTaskRepo.kt
│   └── IPortfolioRepo.kt
├── enums/
└── simple/
```

---

## Domain Models

### ProjectTask

**Path:** `models/task/ProjectTask.kt`

**Purpose:** Represents a task in the project plan.

```kotlin
data class ProjectTask(
    val id: TaskId = TaskId.NONE,
    val title: Title = Title.NONE,
    val description: Description = Description.NONE,
    val duration: Duration = Duration.ZERO,
    val status: ProjectTaskStatus = ProjectTaskStatus.EMPTY,
)
```

**Properties:**
- `id: TaskId` - Unique identifier
- `title: Title` - Task title
- `description: Description` - Task description
- `duration: Duration` - Task duration
- `status: ProjectTaskStatus` - Current status

---

### TaskSchedule

**Path:** `models/taskSchedule/TaskSchedule.kt`

**Purpose:** Represents the scheduled dates for a task.

```kotlin
data class TaskSchedule(
    val id: TaskScheduleId,
    val start: ProjectDate,
    val end: ProjectDate,
)
```

**Properties:**
- `id: TaskScheduleId` - Schedule identifier (matches task ID)
- `start: ProjectDate` - Scheduled start date
- `end: ProjectDate` - Scheduled end date

---

### TaskDependency

**Path:** `models/TaskDependency.kt`

**Purpose:** Represents a dependency between two tasks.

```kotlin
data class TaskDependency(
    val predecessor: TaskId,
    val successor: TaskId,
    val type: DependencyType,
    val lag: Duration,
)
```

**Properties:**
- `predecessor: TaskId` - Predecessor task ID
- `successor: TaskId` - Successor task ID
- `type: DependencyType` - Type of dependency (FS, SS, FF, SF)
- `lag: Duration` - Lag time between tasks

---

### ProjectPlan

**Path:** `models/projectPlan/ProjectPlan.kt`

**Purpose:** Represents the complete project plan with tasks, schedules, and dependencies. Note: Project-level metadata (name, portfolio, priority) has been moved to the `Project` class.

```kotlin
data class ProjectPlan(
    val id: ProjectPlanId,
    private val tasks: MutableMap<TaskId, ProjectTask>,
    private val schedules: MutableMap<TaskScheduleId, TaskSchedule>,
    private val dependencies: MutableSet<TaskDependency>,
) {
    fun tasks(): List<ProjectTask> = tasks.values.toList()
    fun schedules(): Map<TaskScheduleId, TaskSchedule> = schedules.toMap()
    fun dependencies(): List<TaskDependency> = dependencies.toList()

    fun changeTaskStartDate(...): ScheduleDelta { ... }
    fun changeTaskEndDate(...): ScheduleDelta { ... }
    fun addDependency(...): ScheduleDelta { ... }
}
```

**Properties:**
- `id: ProjectPlanId` - Unique identifier for the project plan
- `tasks: MutableMap<TaskId, ProjectTask>` - Map of tasks by ID
- `schedules: MutableMap<TaskScheduleId, TaskSchedule>` - Map of schedules by ID
- `dependencies: MutableSet<TaskDependency>` - Set of task dependencies

**Note:** The following fields were **removed** from `ProjectPlan` and moved to `Project`:
- ~~`name: String`~~ → moved to `Project.name`
- ~~`portfolioId: PortfolioId`~~ → moved to `Project.portfolioId`
- ~~`priority: Int`~~ → moved to `Project.priority`

**Key Methods:**
- `changeTaskStartDate()` - Update task start and recalculate
- `changeTaskEndDate()` - Update task end and recalculate
- `addDependency()` - Add dependency and recalculate schedules
- `planFromEnd()` - Plan task backwards from end date (deadline-driven scheduling)
- `recalculateAll()` - Recalculate all task schedules based on dependencies
- `calculateConstrainedStart()` - Calculate task start based on dependency constraints (FS, SS, FF, SF with lag)

---

### Project

**Path:** `project/Project.kt`

**Purpose:** Represents a project with its metadata. Project-level metadata (name, portfolio, priority) was moved here from `ProjectPlan`.

```kotlin
data class Project(
    val id: ProjectId = ProjectId.NONE,
    val name: ProjectName = ProjectName.NONE,
    val portfolioId: PortfolioId = PortfolioId.NONE,
    val priority: Int = 0,
)
```

**Properties:**
- `id: ProjectId` - Unique identifier for the project
- `name: ProjectName` - Project name
- `portfolioId: PortfolioId` - Reference to the parent portfolio
- `priority: Int` - Project priority (for ordering within portfolio)

**Note:** This class was introduced to separate project-level metadata from the task-focused `ProjectPlan`. The `ProjectPlan` now focuses solely on tasks, schedules, and dependencies.

---

### ProjectPlan Method Details

#### planFromEnd()

**Purpose:** Calculate a task's start date by working backwards from a specified end date.

**Signature:**
```kotlin
fun planFromEnd(
    taskId: TaskId,
    newEnd: LocalDate,
    calendar: TimelineCalendar
): ScheduleDelta
```

**Algorithm:**
1. Get task duration
2. Calculate start date: `calendar.subtractWorkingDays(newEnd, duration)`
3. Update task schedule with new start and end dates
4. Return `ScheduleDelta` with updated schedules

**Use Case:** Deadline-driven scheduling where end date is fixed.

---

#### recalculateAll()

**Purpose:** Recalculate all task schedules based on dependencies and constraints.

**Signature:**
```kotlin
fun recalculateAll(calendar: TimelineCalendar): ScheduleDelta
```

**Algorithm:**
1. Build dependency graph
2. Find root tasks (no predecessors)
3. Topologically sort tasks
4. For each task, call `calculateConstrainedStart()` to apply dependency constraints
5. Collect all schedule changes
6. Return `ScheduleDelta` with updated schedules

**Use Case:** Bulk recalculation after dependency changes or batch updates.

---

#### calculateConstrainedStart()

**Purpose:** Calculate a task's start date based on all predecessor dependencies.

**Signature:**
```kotlin
fun calculateConstrainedStart(
    taskId: TaskId,
    calendar: TimelineCalendar
): LocalDate
```

**Supported Dependency Types:**
- **FS (Finish-to-Start):** `start = predecessor.end + lag`
- **SS (Start-to-Start):** `start = predecessor.start + lag`
- **FF (Finish-to-Finish):** `start = predecessor.end + lag - task.duration`
- **SF (Start-to-Finish):** `start = predecessor.start + lag - task.duration`

**Features:**
- Supports negative lag (overlap)
- Respects working days and holidays via calendar
- Returns earliest valid start date

---

#### resizeTaskFromStart()

**Purpose:** Resize a task from its start date while keeping the end date fixed. This changes the task duration.

**Signature:**
```kotlin
fun resizeTaskFromStart(
    taskId: TaskId,
    newStart: LocalDate,
    calendar: TimelineCalendar
): ScheduleDelta
```

**Algorithm:**
1. Get task's current end date
2. Calculate new duration: `duration = end - newStart`
3. Update task duration in domain model
4. Update task schedule with new start date
5. Apply dependency constraints (FS/SS clamping)
6. Recalculate successor schedules if needed
7. Return `ScheduleDelta` with updated schedules

**Features:**
- Preserves end date while changing start date
- Updates task duration to match new span
- Respects dependency constraints (clamps to FS/SS constraints)
- Triggers cascade recalculation for dependent tasks

**Use Case:** Dragging the left edge of a task bar in the Gantt chart.

---

#### changeDependencyType()

**Purpose:** Change the type of an existing dependency (e.g., from FS to SS) and recalculate affected schedules.

**Signature:**
```kotlin
fun changeDependencyType(
    predecessorId: TaskId,
    successorId: TaskId,
    newType: DependencyType,
    calendar: TimelineCalendar
): ScheduleDelta
```

**Algorithm:**
1. Find existing dependency between predecessor and successor
2. Update dependency type
3. Recalculate lag based on new type (default: SF→1, others→0)
4. Recalculate successor's constrained start using `calculateConstrainedStart()`
5. Cascade recalculation to all dependent tasks
6. Return `ScheduleDelta` with updated schedules

**Features:**
- Updates dependency type in domain model
- Recalculates lag based on new type
- Triggers cascade recalculation for all affected tasks
- Returns complete schedule delta

**Use Case:** Changing dependency type via `DependencyActionPopover` in the UI.

---

#### removeDependency()

**Purpose:** Remove a dependency between two tasks and recalculate affected schedules.

**Signature:**
```kotlin
fun removeDependency(
    predecessorId: TaskId,
    successorId: TaskId,
    calendar: TimelineCalendar
): ScheduleDelta
```

**Algorithm:**
1. Find and remove dependency from list
2. Recalculate successor's start date (may move earlier)
3. Cascade recalculation to all tasks that depended on successor
4. Return `ScheduleDelta` with updated schedules

**Features:**
- Removes dependency constraint
- Successor may move to earlier start date
- Cascade recalculation propagates changes

**Use Case:** Deleting a dependency via the UI.

---

---

### ProjectDate

**Path:** `models/ProjectDate.kt`

**Purpose:** Represents a date that can be set or unset.

```kotlin
sealed class ProjectDate {
    object Unset : ProjectDate()
    data class Set(val date: LocalDate) : ProjectDate()
}
```

**Use Case:** Distinguishes between "no date scheduled" and "a specific date".

---

### Duration

**Path:** `models/simple/Duration.kt`

**Purpose:** Value object representing a duration in days.

```kotlin
@JvmInline
value class Duration(private val days: Int) {
    companion object {
        val ZERO = Duration(0)
        val NONE = Duration(-1)
    }
    
    fun asInt(): Int = days
}
```

---

### TaskId, Title, Description

**Path:** `models/task/simple/`

**Purpose:** Value objects for type safety.

```kotlin
@JvmInline
value class TaskId(val value: String) {
    companion object {
        val NONE = TaskId("")
    }
}

@JvmInline
value class Title(val value: String) {
    companion object {
        val NONE = Title("")
    }
}

@JvmInline
value class Description(val value: String) {
    companion object {
        val NONE = Description("")
    }
}
```

---

## Repository Interface

### IAtlasProjectTaskRepo

**Path:** `repo/IAtlasProjectTaskRepo.kt`

**Purpose:** Defines the contract for task repository operations.

```kotlin
interface IAtlasProjectTaskRepo {
    // Task operations
    fun createTask(task: ProjectTask): ProjectTask
    fun createTaskWithoutSchedule(task: ProjectTask): ProjectTask
    fun getTask(id: String): ProjectTask?
    fun updateTask(task: ProjectTask): ProjectTask
    suspend fun deleteTask(id: String): Int

    // Schedule operations
    fun updateSchedule(schedule: TaskSchedule)

    // Dependency operations
    fun addDependency(predecessorId: String, successorId: String,
                      type: String, lagDays: Int)
    fun updateDependency(predecessorId: String, successorId: String,
                         type: String, lagDays: Int)
    fun deleteDependency(predecessorId: String, successorId: String)

    // Project plan
    fun projectPlan(): ProjectPlan

    // Task count
    suspend fun countTasks(planId: String): Int

    // Baseline operations
    suspend fun saveBaseline(planId: String)
}
```

**New Methods:**
- `countTasks(planId: String): Int` - Count the number of tasks in a project plan (added 2026-03-22)
- `saveBaseline(planId: String)` - Save a baseline snapshot of the project plan

**Implementations:**
- `PostgresRepo` - PostgreSQL implementation with cascade delete
- `InMemoryRepo` - In-memory implementation for testing

**Delete Operation:**
- Returns the number of tasks deleted (typically 1)
- Implementations should cascade delete associated schedules and dependencies
- Returns 0 if task not found (implementations should check existence)

---

### IPortfolioRepo

**Path:** `repo/IPortfolioRepo.kt`

**Purpose:** Defines the contract for portfolio and project repository operations. This interface manages portfolios and their associated projects.

```kotlin
interface IPortfolioRepo {
    // Portfolio operations
    suspend fun listPortfolios(): List<Portfolio>
    suspend fun getPortfolio(id: String): Portfolio?
    suspend fun createPortfolio(name: String, description: String?): String
    suspend fun updatePortfolio(portfolioId: String, name: String?, description: String?): Int
    suspend fun deletePortfolio(id: String): Int

    // Project operations
    suspend fun listProjects(portfolioId: String): List<Project>
    suspend fun getProject(id: String): Project?
    suspend fun createProject(portfolioId: String, name: String, priority: Int): Project
    suspend fun updateProject(projectId: String, name: String?, priority: Int?): Int
    suspend fun deleteProject(projectId: String): Int
    suspend fun reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int
    suspend fun listAllProjects(): List<Project>
}
```

**Methods:**

| Method | Description |
|--------|-------------|
| `listPortfolios()` | List all portfolios |
| `getPortfolio(id)` | Get a specific portfolio by ID |
| `createPortfolio(name, description)` | Create a new portfolio |
| `updatePortfolio(id, name, description)` | Update portfolio metadata |
| `deletePortfolio(id)` | Delete a portfolio |
| `listProjects(portfolioId)` | List all projects in a portfolio (returns `List<Project>`) |
| `getProject(id)` | Get a specific project by ID |
| `createProject(portfolioId, name, priority)` | Create a new project in a portfolio (returns `Project`) |
| `updateProject(projectId, name, priority)` | Update project metadata |
| `deleteProject(projectId)` | Delete a project |
| `reorderProjects(portfolioId, orderedIds)` | Reorder projects within a portfolio |
| `listAllProjects()` | List all projects across all portfolios |

**Note:** The following changes were made on 2026-03-22:
- `listProjectIds()` → `listProjects()` - Now returns `List<Project>` instead of `List<String>`
- `getProject(id)` - **Added** - Get a single project by ID
- `createProject()` - Now returns `Project` instead of `String` (project ID)
- `listAllProjectIds()` → `listAllProjects()` - Now returns `List<Project>` instead of `List<Pair<String, String>>`

**Implementations:**
- `PortfolioRepoPostgres` - PostgreSQL implementation
- `PortfolioRepoInMemory` - In-memory implementation for testing

---

## Resource Models

### Resource

**Path:** `models/resource/Resource.kt`

**Purpose:** Represents a resource (person or role) available for task work.

```kotlin
data class Resource(
    val id: ResourceId = ResourceId.NONE,
    val name: ResourceName = ResourceName.NONE,
    val type: ResourceType = ResourceType.PERSON,
    val capacityHoursPerDay: Double = 8.0,
    val sortOrder: Int = 0,
)
```

**Properties:**
- `id: ResourceId` - Unique identifier
- `name: ResourceName` - Resource name
- `type: ResourceType` - Either PERSON or ROLE
- `capacityHoursPerDay: Double` - Default daily capacity
- `sortOrder: Int` - Display order

---

### TaskAssignment

**Path:** `models/resource/TaskAssignment.kt`

**Purpose:** Represents an assignment of a resource to a task.

```kotlin
data class TaskAssignment(
    val id: AssignmentId = AssignmentId.NONE,
    val taskId: TaskId = TaskId.NONE,
    val resourceId: ResourceId = ResourceId.NONE,
    val hoursPerDay: Double = 8.0,
    val plannedEffortHours: Double? = null,
)
```

**Properties:**
- `id: AssignmentId` - Unique assignment identifier
- `taskId: TaskId` - Reference to the task
- `resourceId: ResourceId` - Reference to the resource
- `hoursPerDay: Double` - Daily allocation
- `plannedEffortHours: Double?` - Optional total effort estimate

---

### ResourceCalendarOverride

**Path:** `models/resource/ResourceCalendarOverride.kt`

**Purpose:** Overrides a resource's capacity on a specific date.

```kotlin
data class ResourceCalendarOverride(
    val resourceId: ResourceId,
    val date: LocalDate,
    val availableHours: Double,
)
```

---

### AssignmentDayOverride

**Path:** `models/resource/AssignmentDayOverride.kt`

**Purpose:** Overrides an assignment's hours on a specific date.

```kotlin
data class AssignmentDayOverride(
    val assignmentId: AssignmentId,
    val date: LocalDate,
    val hours: Double,
)
```

---

### ResourceLevelingEngine

**Path:** `models/resource/ResourceLevelingEngine.kt`

**Purpose:** Automatic resource overload resolution engine.

```kotlin
class ResourceLevelingEngine(
    private val plan: ProjectPlan,
    private val assignments: List<TaskAssignment>,
    private val resources: List<Resource>,
    private val calendar: TimelineCalendar,
    private val calendarOverrides: Map<ResourceId, List<ResourceCalendarOverride>>,
    private val dayOverridesByAssignment: Map<AssignmentId, List<AssignmentDayOverride>>,
) {
    fun level(): LevelingResult { ... }
}
```

**Algorithm:**
1. Compute resource load for all resources
2. Identify overloaded days
3. Delay non-critical tasks to resolve overloads
4. Return leveling result with updated schedules

---

### ResourceLoadCalculator

**Path:** `models/resource/ResourceLoadCalculator.kt`

**Purpose:** Calculates resource load and generates overload reports.

```kotlin
class ResourceLoadCalculator(...) {
    fun computeLoad(from: LocalDate, to: LocalDate): OverloadReport { ... }
    fun computeResourceLoad(resourceId: ResourceId, from: LocalDate, to: LocalDate): ResourceLoadResult { ... }
}
```

---

## Repository Interfaces

### IResourceRepo

**Path:** `repo/IResourceRepo.kt`

**Purpose:** Defines the contract for resource repository operations.

```kotlin
interface IResourceRepo {
    // Resource operations
    fun listResources(planId: String): List<Resource>
    fun createResource(planId: String, resource: Resource): Resource
    fun getResource(id: String): Resource?
    fun updateResource(resource: Resource): Resource
    fun deleteResource(id: String)
    
    // Calendar override operations
    fun getCalendarOverrides(resourceId: String): List<ResourceCalendarOverride>
    fun setCalendarOverride(override: ResourceCalendarOverride)
    fun deleteCalendarOverride(resourceId: String, date: LocalDate)
    
    // Assignment operations
    fun listAssignments(planId: String): List<TaskAssignment>
    fun createAssignment(planId: String, assignment: TaskAssignment): TaskAssignment
    fun updateAssignment(id: String, hoursPerDay: Double, plannedEffortHours: Double?): TaskAssignment
    fun deleteAssignment(id: String)
    
    // Day override operations
    fun getAssignmentDayOverrides(assignmentId: String): List<AssignmentDayOverride>
    fun setAssignmentDayOverride(override: AssignmentDayOverride)
    fun deleteAssignmentDayOverride(assignmentId: String, date: LocalDate)
    
    // Bulk operations
    fun getAllDayOverridesForPlan(planId: String): List<AssignmentDayOverride>
}
```

---

## Enums

### ProjectTaskStatus

**Path:** `models/task/enums/`

```kotlin
enum class ProjectTaskStatus {
    EMPTY,
    IN_PROGRESS,
    DONE
}
```

---

### DependencyType

**Path:** `models/task/enums/` (or similar)

```kotlin
enum class DependencyType {
    FS,  // Finish-to-Start
    SS,  // Start-to-Start
    FF,  // Finish-to-Finish
    SF   // Start-to-Finish
}
```

---

## Dependencies

**Imports:**
- `kotlinx.datetime.LocalDate` for date handling

**Imported by:**
- All other backend modules
- Transport module (for DTO mapping)
- Mappers module (for conversions)
- Repository implementations

---

## Related Files

- [Transport Module](./transport.md)
- [Mappers Module](./mappers.md)
- [Postgres Module](./postgres.md)
- [Repo In-Memory](./repo-in-memory.md)
