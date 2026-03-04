# Repo In-Memory Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-repo-in-memory/`  
**Module:** [Backend Index](../INDEX.md)  
**Last Updated:** 2026-03-03

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
