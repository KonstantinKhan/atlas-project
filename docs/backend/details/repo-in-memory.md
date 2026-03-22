# Repo In-Memory Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-repo-in-memory/`
**Module:** [Backend Index](../INDEX.md)
**Last Updated:** 2026-03-22

## Purpose

The Repo In-Memory module provides an in-memory implementation of the repository interface. It is primarily used for testing, development, and prototyping without requiring a database connection.

## Directory Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/repo/inmemory/
└── (In-Memory repository implementation)
```

---

## Repository Implementation

### InMemoryRepo (implementation of IAtlasProjectTaskRepo)

**Path:** `repo/inmemory/`

**Purpose:** Implements the repository interface using in-memory data structures.

---

### Key Responsibilities

1. **Task Storage:** In-memory CRUD operations for project tasks
2. **Schedule Storage:** In-memory task schedule management
3. **Dependency Storage:** In-memory dependency management
4. **Project Plan Assembly:** Build project plan from memory

---

### Data Structures

```kotlin
class InMemoryRepo : IAtlasProjectTaskRepo {
    private val tasks = mutableMapOf<TaskId, ProjectTask>()
    private val schedules = mutableMapOf<TaskScheduleId, TaskSchedule>()
    private val dependencies = mutableListOf<TaskDependency>()
    private var nextId = 1
}
```

---

### Interface Implementation

```kotlin
class InMemoryRepo : IAtlasProjectTaskRepo {
    
    // Task operations
    override fun createTask(task: ProjectTask): ProjectTask { ... }
    override fun createTaskWithoutSchedule(task: ProjectTask): ProjectTask { ... }
    override fun getTask(id: String): ProjectTask? { ... }
    override fun updateTask(task: ProjectTask): ProjectTask { ... }
    
    // Schedule operations
    override fun updateSchedule(schedule: TaskSchedule) { ... }
    
    // Dependency operations
    override fun addDependency(predecessorId: String, successorId: String, 
                               type: String, lagDays: Int) { ... }
    
    // Project plan
    override fun projectPlan(): ProjectPlan { ... }
}
```

---

### Task Operations

#### createTask(task: ProjectTask): ProjectTask

**Purpose:** Create a new task with an auto-generated ID.

**Implementation:**
```kotlin
override fun createTask(task: ProjectTask): ProjectTask {
    val newId = TaskId("task-${nextId++}")
    val newTask = task.copy(id = newId)
    tasks[newId] = newTask
    return newTask
}
```

---

#### createTaskWithoutSchedule(task: ProjectTask): ProjectTask

**Purpose:** Create a task in the pool without scheduling it.

**Implementation:**
```kotlin
override fun createTaskWithoutSchedule(task: ProjectTask): ProjectTask {
    return createTask(task)  // Same as createTask for in-memory
}
```

---

#### getTask(id: String): ProjectTask?

**Purpose:** Retrieve a task by ID.

**Implementation:**
```kotlin
override fun getTask(id: String): ProjectTask? {
    return tasks[TaskId(id)]
}
```

---

#### updateTask(task: ProjectTask): ProjectTask

**Purpose:** Update an existing task.

**Implementation:**
```kotlin
override fun updateTask(task: ProjectTask): ProjectTask {
    tasks[task.id] = task
    return task
}
```

---

#### deleteTask(id: String): Int

**Purpose:** Delete a task by ID.

**Implementation:**
```kotlin
override suspend fun deleteTask(id: String): Int = newSuspendedTransaction(db = database) {
    ProjectTasksTable.deleteWhere { ProjectTasksTable.id eq id }
}
```

**Note:** The in-memory implementation uses Exposed's `deleteWhere` for consistency with the PostgreSQL implementation, but operates on an in-memory H2 database.

**Returns:** Number of tasks deleted (1 if successful, 0 if not found)

---

### Schedule Operations

#### updateSchedule(schedule: TaskSchedule)

**Purpose:** Insert or update a task schedule.

**Implementation:**
```kotlin
override fun updateSchedule(schedule: TaskSchedule) {
    schedules[schedule.id] = schedule
}
```

---

### Dependency Operations

#### addDependency(predecessorId, successorId, type, lagDays)

**Purpose:** Create a dependency between two tasks.

**Implementation:**
```kotlin
override fun addDependency(
    predecessorId: String, 
    successorId: String, 
    type: String, 
    lagDays: Int
) {
    val dependency = TaskDependency(
        predecessor = TaskId(predecessorId),
        successor = TaskId(successorId),
        type = DependencyType.valueOf(type),
        lag = Duration(lagDays)
    )
    dependencies.add(dependency)
}
```

---

### Project Plan Assembly

#### projectPlan(): ProjectPlan

**Purpose:** Build the complete project plan from memory.

**Implementation:**
```kotlin
override fun projectPlan(): ProjectPlan {
    return ProjectPlan(
        id = ProjectPlanId("main"),
        tasks = tasks.toMutableMap(),
        schedules = schedules.toMutableMap(),
        dependencies = dependencies.toMutableList()
    )
}
```

---

#### countTasks(planId: String): Int

**Purpose:** Count the number of tasks in a project plan.

**Implementation:**
```kotlin
override suspend fun countTasks(planId: String): Int {
    TODO("Not yet implemented - in-memory repo does not support countTasks")
}
```

**Note:** This method is not yet implemented in the in-memory repository. It is a stub that throws `TODO` exception.

---

## Portfolio Repository Implementation

### PortfolioRepoInMemory (implementation of IPortfolioRepo)

**Path:** `repo/inmemory/`

**Purpose:** Implements the portfolio repository interface using in-memory data structures. Used for testing and development.

---

### Data Structures

```kotlin
class PortfolioRepoInMemory : IPortfolioRepo {
    private val portfolios = mutableMapOf<String, PortfolioEntry>()
    private val projects = mutableMapOf<String, ProjectEntry>()

    private data class PortfolioEntry(
        val id: String,
        val name: String,
        val description: String?,
    )

    private data class ProjectEntry(
        val portfolioId: String,
        val name: String,
        val priority: Int,
    ) {
        fun toProject(id: String) = Project(
            id = ProjectId(id),
            name = ProjectName(name),
            portfolioId = PortfolioId(portfolioId),
            priority = priority,
        )
    }
}
```

---

### Interface Implementation

```kotlin
class PortfolioRepoInMemory : IPortfolioRepo {

    // Portfolio operations
    override suspend fun listPortfolios(): List<Portfolio> { ... }
    override suspend fun getPortfolio(id: String): Portfolio? { ... }
    override suspend fun createPortfolio(name: String, description: String?): String { ... }
    override suspend fun updatePortfolio(portfolioId: String, name: String?, description: String?): Int { ... }
    override suspend fun deletePortfolio(id: String): Int { ... }

    // Project operations
    override suspend fun listProjects(portfolioId: String): List<Project> { ... }
    override suspend fun getProject(id: String): Project? { ... }
    override suspend fun createProject(portfolioId: String, name: String, priority: Int): Project { ... }
    override suspend fun updateProject(projectId: String, name: String?, priority: Int?): Int { ... }
    override suspend fun deleteProject(projectId: String): Int { ... }
    override suspend fun reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int { ... }
    override suspend fun listAllProjects(): List<Project> { ... }
}
```

---

### Portfolio Operations

#### listPortfolios(): List<Portfolio>

**Purpose:** List all portfolios.

**Implementation:**
```kotlin
override suspend fun listPortfolios(): List<Portfolio> =
    portfolios.values.toList().map { it.toPortfolio() }
```

---

#### getPortfolio(id: String): Portfolio?

**Purpose:** Get a specific portfolio by ID.

**Implementation:**
```kotlin
override suspend fun getPortfolio(id: String): Portfolio? =
    portfolios[id]?.toPortfolio()
```

---

#### createPortfolio(name: String, description: String?): String

**Purpose:** Create a new portfolio.

**Implementation:**
```kotlin
@OptIn(ExperimentalUuidApi::class)
override suspend fun createPortfolio(name: String, description: String?): String {
    val newId = Uuid.random().toString()
    portfolios[newId] = PortfolioEntry(id = newId, name = name, description = description)
    return newId
}
```

---

#### updatePortfolio(portfolioId: String, name: String?, description: String?): Int

**Purpose:** Update portfolio metadata.

---

#### deletePortfolio(id: String): Int

**Purpose:** Delete a portfolio.

**Implementation:**
```kotlin
override suspend fun deletePortfolio(id: String): Int =
    if (portfolios.remove(id) != null) 1 else 0
```

---

### Project Operations

#### listProjects(portfolioId: String): List<Project>

**Purpose:** List all projects in a portfolio, ordered by priority.

**Implementation:**
```kotlin
override suspend fun listProjects(portfolioId: String): List<Project> =
    projects.filter { it.value.portfolioId == portfolioId }
        .entries.sortedBy { it.value.priority }
        .map { (id, entry) -> entry.toProject(id) }
```

**Returns:** `List<Project>` with id, name, portfolioId, and priority

**Note:** Changed on 2026-03-22 - previously returned `List<String>` (project IDs only)

---

#### getProject(id: String): Project?

**Purpose:** Get a specific project by ID.

**Implementation:**
```kotlin
override suspend fun getProject(id: String): Project? =
    projects[id]?.toProject(id)
```

**Returns:** `Project` if found, null otherwise

**Note:** Added on 2026-03-22

---

#### createProject(portfolioId: String, name: String, priority: Int): Project

**Purpose:** Create a new project in a portfolio.

**Implementation:**
```kotlin
@OptIn(ExperimentalUuidApi::class)
override suspend fun createProject(portfolioId: String, name: String, priority: Int): Project {
    val newId = Uuid.random().toString()
    val entry = ProjectEntry(portfolioId = portfolioId, name = name, priority = priority)
    projects[newId] = entry
    return entry.toProject(newId)
}
```

**Returns:** `Project` object with all fields

**Note:** Changed on 2026-03-22 - previously returned `String` (project ID only)

---

#### updateProject(projectId: String, name: String?, priority: Int?): Int

**Purpose:** Update project metadata.

---

#### deleteProject(projectId: String): Int

**Purpose:** Delete a project.

**Implementation:**
```kotlin
override suspend fun deleteProject(projectId: String): Int =
    if (projects.remove(projectId) != null) 1 else 0
```

---

#### reorderProjects(portfolioId: String, orderedProjectIds: List<String>): Int

**Purpose:** Update project priorities based on ordered list.

---

#### listAllProjects(): List<Project>

**Purpose:** List all projects across all portfolios.

**Implementation:**
```kotlin
override suspend fun listAllProjects(): List<Project> =
    projects.map { (id, entry) -> entry.toProject(id) }
```

**Returns:** `List<Project>` with all projects

**Note:** Changed on 2026-03-22 - previously returned `List<Pair<String, String>>` (portfolioId to projectId pairs)

---

### Helper Methods

#### toProject(id: String): Project

**Purpose:** Convert a ProjectEntry to a Project domain object.

**Implementation:**
```kotlin
fun toProject(id: String) = Project(
    id = ProjectId(id),
    name = ProjectName(name),
    portfolioId = PortfolioId(portfolioId),
    priority = priority,
)
```

---

## Use Cases

### Development

Run the application without setting up a database:

```kotlin
// In AppConfig
val repo: IAtlasProjectTaskRepo = InMemoryRepo()
```

### Testing

Unit tests without database dependencies:

```kotlin
@Test
fun `test task creation`() {
    val repo = InMemoryRepo()
    val task = ProjectTask(title = Title("Test"))
    
    val created = repo.createTask(task)
    
    assertNotNull(created.id)
    assertEquals("Test", created.title.value)
}
```

### Prototyping

Quickly prototype features before implementing persistence.

---

## Advantages

| Advantage | Description |
|-----------|-------------|
| **No Database Required** | Run without PostgreSQL setup |
| **Fast** | In-memory operations are faster than database |
| **Isolated** | Each test gets a fresh repository |
| **Simple** | No SQL, no migrations, no connection pooling |

---

## Limitations

| Limitation | Description |
|------------|-------------|
| **No Persistence** | Data lost on application restart |
| **No Concurrency** | Not thread-safe for production use |
| **No Querying** | Limited to simple lookups |
| **No Transactions** | No ACID guarantees |

---

## Dependencies

**Imports:**
- Domain models from `atlas-project-backend-common`
- Repository interface from `atlas-project-backend-common/repo`

**Imported by:**
- Ktor App module (for development/testing)
- Test suites

---

## Related Files

- [Common Module](./common.md)
- [Postgres Module](./postgres.md)
- [Ktor App Module](./ktor-app.md)
