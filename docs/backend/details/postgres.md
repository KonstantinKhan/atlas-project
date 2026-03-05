# Postgres Module - Detail

**Path:** `/backend/atlas-project-backend/atlas-project-backend-postgres/`
**Module:** [Backend Index](../INDEX.md)
**Last Updated:** 2026-03-04

## Purpose

The Postgres module provides the PostgreSQL implementation of the repository interface. It handles persistent storage of tasks, schedules, and dependencies.

## Directory Structure

```
src/main/kotlin/com/khan366kos/atlas/project/backend/repo/postgres/
├── AtlasProjectTaskRepoPostgres.kt    # Main repository implementation
├── ProjectTasksTable.kt               # Task table definition
├── mapper/
│   ├── TimelineCalendarMapper.kt
│   ├── TimelineCalendarHolidayMapper.kt
│   └── TimelineCalendarWorkingWeekendMapper.kt
└── table/
    ├── ProjectPlansTable.kt
    ├── TaskSchedulesTable.kt
    ├── TaskDependenciesTable.kt
    ├── TimelineCalendarTable.kt
    ├── TimelineCalendarHolidaysTable.kt
    └── TimelineCalendarWorkingWeekendsTable.kt
```

---

## Repository Implementation

### PostgresRepo (implementation of IAtlasProjectTaskRepo)

**Path:** `repo/postgres/`

**Purpose:** Implements the repository interface using PostgreSQL as the data store.

---

### Key Responsibilities

1. **Task Persistence:** CRUD operations for project tasks
2. **Schedule Persistence:** Store and update task schedules
3. **Dependency Persistence:** Store task dependencies
4. **Project Plan Assembly:** Build complete project plan from database

---

### Interface Implementation

```kotlin
class PostgresRepo(
    dataSource: DataSource
) : IAtlasProjectTaskRepo {
    
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

**SQL:**
```sql
INSERT INTO project_tasks (id, title, description, duration, status)
VALUES (?, ?, ?, ?, ?)
RETURNING *
```

**Returns:** Created task with assigned ID

---

#### createTaskWithoutSchedule(task: ProjectTask): ProjectTask

**Purpose:** Create a task in the pool without scheduling it.

**SQL:**
```sql
INSERT INTO project_tasks (id, title, description, duration, status)
VALUES (?, ?, ?, ?, ?)
RETURNING *
```

---

#### getTask(id: String): ProjectTask?

**Purpose:** Retrieve a task by ID.

**SQL:**
```sql
SELECT * FROM project_tasks WHERE id = ?
```

**Returns:** Task if found, null otherwise

---

#### updateTask(task: ProjectTask): ProjectTask

**Purpose:** Update an existing task.

**SQL:**
```sql
UPDATE project_tasks
SET title = ?, description = ?, duration = ?, status = ?
WHERE id = ?
RETURNING *
```

---

#### deleteTask(id: String): Int

**Purpose:** Delete a task and all associated schedules and dependencies.

**SQL:**
```sql
-- Delete associated schedules first
DELETE FROM task_schedules WHERE task_id = ?

-- Delete associated dependencies (both as predecessor and successor)
DELETE FROM task_dependencies 
WHERE predecessor_id = ? OR successor_id = ?

-- Delete the task itself
DELETE FROM project_tasks WHERE id = ?
```

**Implementation:**
```kotlin
override suspend fun deleteTask(id: String): Int = newSuspendedTransaction(db = database) {
    val uuid = UUID.fromString(id)
    TaskSchedulesTable.deleteWhere { taskId eq uuid }
    TaskDependenciesTable.deleteWhere { (predecessorTaskId eq uuid) or (successorTaskId eq uuid) }
    ProjectTasksTable.deleteWhere { ProjectTasksTable.id eq uuid }
}
```

**Cascade Delete:**
- All task schedules are deleted first
- All dependencies where the task is either predecessor or successor are deleted
- Finally, the task itself is deleted
- All operations are wrapped in a single transaction for atomicity

**Returns:** Number of tasks deleted (1 if successful, 0 if not found)

---

### Schedule Operations

#### updateSchedule(schedule: TaskSchedule)

**Purpose:** Insert or update a task schedule.

**SQL:**
```sql
INSERT INTO task_schedules (task_id, start_date, end_date)
VALUES (?, ?, ?)
ON CONFLICT (task_id) DO UPDATE
SET start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date
```

---

### Dependency Operations

#### addDependency(predecessorId, successorId, type, lagDays)

**Purpose:** Create a dependency between two tasks.

**SQL:**
```sql
INSERT INTO task_dependencies (predecessor_id, successor_id, type, lag_days)
VALUES (?, ?, ?, ?)
```

---

### Project Plan Assembly

#### projectPlan(): ProjectPlan

**Purpose:** Build the complete project plan from database.

**SQL Queries:**
```sql
-- Fetch all tasks
SELECT * FROM project_tasks

-- Fetch all schedules
SELECT * FROM task_schedules

-- Fetch all dependencies
SELECT * FROM task_dependencies
```

**Assembly:**
```kotlin
override fun projectPlan(): ProjectPlan {
    val tasks = fetchAllTasks().associateBy { it.id }
    val schedules = fetchAllSchedules().associateBy { it.id }
    val dependencies = fetchAllDependencies()
    
    return ProjectPlan(
        id = ProjectPlanId("main"),
        tasks = tasks,
        schedules = schedules,
        dependencies = dependencies
    )
}
```

---

## Database Schema

### project_tasks Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(36) | PRIMARY KEY |
| title | VARCHAR(255) | NOT NULL |
| description | TEXT | |
| duration | INTEGER | NOT NULL |
| status | VARCHAR(20) | NOT NULL |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | |

---

### task_schedules Table

| Column | Type | Constraints |
|--------|------|-------------|
| task_id | VARCHAR(36) | PRIMARY KEY, FK → project_tasks |
| start_date | DATE | |
| end_date | DATE | |

---

### task_dependencies Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | SERIAL | PRIMARY KEY |
| predecessor_id | VARCHAR(36) | FK → project_tasks |
| successor_id | VARCHAR(36) | FK → project_tasks |
| type | VARCHAR(2) | NOT NULL (FS, SS, FF, SF) |
| lag_days | INTEGER | DEFAULT 0 |

---

### project_plans Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(36) | PRIMARY KEY |
| name | VARCHAR(255) | NOT NULL |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | |

---

### timeline_calendar Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(36) | PRIMARY KEY |
| name | VARCHAR(255) | NOT NULL |
| version | INTEGER | NOT NULL |

---

### timeline_calendar_holidays Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | SERIAL | PRIMARY KEY |
| calendar_id | VARCHAR(36) | FK → timeline_calendar |
| holiday_date | DATE | NOT NULL |

---

### timeline_calendar_working_weekends Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | SERIAL | PRIMARY KEY |
| calendar_id | VARCHAR(36) | FK → timeline_calendar |
| weekend_date | DATE | NOT NULL |

---

## Table Definitions (table/)

### ProjectTasksTable.kt

**Purpose:** Defines the `project_tasks` table schema and Exposed DSL mappings.

**Key Functions:**
- Table definition with id, title, description, duration, status columns
- Object reference to task_schedules and task_dependencies

---

### TaskSchedulesTable.kt

**Purpose:** Defines the `task_schedules` table schema.

**Key Functions:**
- Table definition with task_id (as foreign key), start_date, end_date columns
- Reference to project_tasks table

---

### TaskDependenciesTable.kt

**Purpose:** Defines the `task_dependencies` table schema.

**Key Functions:**
- Table definition with predecessor_id, successor_id, type, lag_days columns
- References to project_tasks for both predecessor and successor

---

### ProjectPlansTable.kt

**Purpose:** Defines the `project_plans` table schema.

**Key Functions:**
- Table definition for storing project plan metadata
- Reference to tasks and schedules

---

### TimelineCalendarTable.kt

**Purpose:** Defines the `timeline_calendar` table schema.

**Key Functions:**
- Table definition for work calendar configuration
- Version tracking for calendar updates

---

### TimelineCalendarHolidaysTable.kt

**Purpose:** Defines the `timeline_calendar_holidays` table schema.

**Key Functions:**
- Stores holiday dates associated with a calendar
- Many-to-one relationship with timeline_calendar

---

### TimelineCalendarWorkingWeekendsTable.kt

**Purpose:** Defines the `timeline_calendar_working_weekends` table schema.

**Key Functions:**
- Stores working weekend dates (weekends that are working days)
- Many-to-one relationship with timeline_calendar

---

## Mapper Classes (mapper/)

### TimelineCalendarMapper.kt

**Purpose:** Maps between database rows and `TimelineCalendar` domain model.

**Key Functions:**
- `mapRow()` - Convert database result to TimelineCalendar
- Handles holidays and working weekends aggregation

---

### TimelineCalendarHolidayMapper.kt

**Purpose:** Maps between database rows and holiday entities.

**Key Functions:**
- `mapRow()` - Convert database row to holiday date
- `toEntity()` - Convert to domain entity

---

### TimelineCalendarWorkingWeekendMapper.kt

**Purpose:** Maps between database rows and working weekend entities.

**Key Functions:**
- `mapRow()` - Convert database row to working weekend date
- `toEntity()` - Convert to domain entity

---

## Dependencies

**Imports:**
- Domain models from `atlas-project-backend-common`
- Repository interface from `atlas-project-backend-common/repo`
- `javax.sql.DataSource` for database connection

**Imported by:**
- Ktor App module (via dependency injection)

---

## Configuration

### Database Connection

Configured via environment variables or application.conf:

```hocon
ktor {
    database {
        url = "jdbc:postgresql://localhost:5432/atlas_project"
        user = "postgres"
        password = "postgres"
    }
}
```

---

## Related Files

- [Common Module](./common.md)
- [Repo In-Memory](./repo-in-memory.md)
- [Ktor App Module](./ktor-app.md)
