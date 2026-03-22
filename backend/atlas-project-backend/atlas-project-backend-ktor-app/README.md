# Backend Ktor Application Module

**Path:** `/backend/atlas-project-backend/atlas-project-backend-ktor-app`
**Last Updated:** 2026-03-22

## Overview

The Ktor application module is the main entry point for the backend REST API. It configures the HTTP server, routing, serialization, database connections, and error handling.

## Purpose

- **HTTP Server**: Ktor with Netty engine
- **API Routing**: Define REST endpoints
- **Configuration**: Database, HTTP, serialization settings
- **Dependency Injection**: Wire together repositories, services, mappers
- **Error Handling**: Global exception handling and status pages

## Structure

```
src/main/kotlin/
├── com/khan366kos/atlas/project/backend/ktor/app/
│   ├── Application.kt                 # Main entry point
│   ├── plugins/
│   │   └── Routing.kt                 # Route configuration
│   └── routes/
│       ├── Analysis.kt                # What-if, blocker chain analysis
│       ├── Assignments.kt             # Task-resource assignments
│       ├── Baselines.kt               # Project baselines
│       ├── CriticalPath.kt            # CPM endpoint
│       ├── Leveling.kt                # Resource leveling
│       ├── Portfolios.kt              # Portfolio management
│       ├── ProjectPlan.kt             # Project plan endpoints
│       ├── ReorderTasks.kt            # Task reordering
│       ├── ResourceLoad.kt            # Resource load calculation
│       └── Resources.kt               # Resource management
├── config/
│   └── AppConfig.kt                   # Application configuration
├── Databases.kt                       # Database setup
├── HTTP.kt                            # HTTP configuration
├── Mappers.kt                         # Global mapper setup
├── Routing.kt                         # Route definitions
├── Serialization.kt                   # JSON configuration
├── StatusPages.kt                     # Error handling
└── WorkCalendarUtils.kt               # Calendar utilities

src/main/resources/
├── application.conf                   # Application configuration
└── db/migration/                      # Flyway migrations
```

## Entry Point

### Application.kt

```kotlin
fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(
    config: AppConfig = AppConfig(environment)
) {
    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureRoutingOld(config)
    configureRouting(config)
}
```

### AppConfig.kt

```kotlin
data class AppConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val port: Int
) {
    constructor(environment: ApplicationEnvironment) : this(
        databaseUrl = environment.config.property("ktor.database.url").getString(),
        databaseUser = environment.config.property("ktor.database.user").getString(),
        databasePassword = environment.config.property("ktor.database.password").getString(),
        port = environment.config.property("ktor.deployment.port").getInt()
    )
}
```

## Configuration Modules

### Serialization (Serialization.kt)

```kotlin
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            serializersModule = SerializersModule {
                // Custom serializers if needed
            }
        })
    }
}
```

### HTTP Configuration (HTTP.kt)

```kotlin
fun Application.configureHTTP() {
    install(CORS) {
        allowHost("localhost:3000")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
}
```

### Status Pages (StatusPages.kt)

```kotlin
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = cause.message ?: "Unknown error",
                status = HttpStatusCode.InternalServerError
            )
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(
                text = status.toString(),
                status = status
            )
        }
    }
}
```

### Database Setup (Databases.kt)

```kotlin
fun Application.configureDatabases(config: AppConfig) {
    Database.connect(
        url = config.databaseUrl,
        user = config.databaseUser,
        password = config.databasePassword,
        driver = "org.postgresql.Driver"
    )
    
    // Run Flyway migrations
    Flyway.configure()
        .dataSource(config.databaseUrl, config.databaseUser, config.databasePassword)
        .load()
        .migrate()
}
```

## Routing

### Main Routing (Routing.kt)

```kotlin
fun Application.configureRoutingOld(config: AppConfig) {
    routing {
        route("/project-tasks") {
            get {
                // Get all tasks
                val tasks = repo.getAll()
                call.respond(tasks.map { it.toDto() })
            }
            
            post("/create-in-pool") {
                // Create new task
                val request = call.receive<CreateProjectTaskRequest>()
                val task = repo.create(request.toDomain())
                call.respond(task.toDto())
            }
            
            patch("/{id}") {
                // Update task
                val id = call.parameters["id"]!!
                val updates = call.receive<UpdateProjectTaskRequest>()
                val task = repo.update(id, updates)
                call.respond(task.toDto())
            }
            
            delete("/{id}") {
                // Delete task
                val id = call.parameters["id"]!!
                repo.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
```

### Project Plan Routes (routes/ProjectPlan.kt)

```kotlin
fun Route.projectPlanRoutes(repo: IAtlasProjectTaskRepo) {
    get("/project-plan") {
        val plan = repo.getProjectPlan()
        call.respond(plan.toDto())
    }
    
    post("/change-start") {
        val command = call.receive<ChangeTaskStartDateCommandDto>()
        val delta = repo.changeStartDate(command)
        call.respond(delta.toDto())
    }
    
    post("/change-end") {
        val command = call.receive<ChangeTaskEndDateCommandDto>()
        val delta = repo.changeEndDate(command)
        call.respond(delta.toDto())
    }
    
    post("/resize-from-start") {
        val command = call.receive<ResizeFromStartCommandDto>()
        val delta = repo.resizeFromStart(command)
        call.respond(delta.toDto())
    }
    
    post("/plan-from-end") {
        val command = call.receive<PlanFromEndCommandDto>()
        val plan = repo.planFromEnd(command)
        call.respond(plan.toDto())
    }
}
```

### Critical Path Routes (routes/CriticalPath.kt)

```kotlin
fun Route.criticalPathRoutes(repo: IAtlasProjectTaskRepo) {
    get("/critical-path") {
        val plan = repo.getProjectPlan()
        val criticalPath = CriticalPathAnalysis.calculate(
            tasks = plan.tasks,
            schedules = plan.schedules,
            dependencies = plan.dependencies
        )
        call.respond(criticalPath.toDto())
    }
}
```

### Dependency Routes

```kotlin
route("/dependencies") {
    post {
        val command = call.receive<CreateDependencyCommandDto>()
        val plan = repo.createDependency(command)
        call.respond(plan.toDto())
    }
    
    delete {
        val from = call.request.queryParameters["from"]!!
        val to = call.request.queryParameters["to"]!!
        val plan = repo.deleteDependency(from, to)
        call.respond(plan.toDto())
    }
    
    patch {
        val command = call.receive<ChangeDependencyTypeCommandDto>()
        val plan = repo.changeDependencyType(command)
        call.respond(plan.toDto())
    }
}
```

### Timeline Calendar Routes

```kotlin
route("/timeline-calendar") {
    get {
        val calendar = repo.getTimelineCalendar()
        call.respond(calendar.toDto())
    }

    put {
        val calendar = call.receive<TimelineCalendarDto>()
        val updated = repo.updateTimelineCalendar(calendar.toDomain())
        call.respond(updated.toDto())
    }
}
```

## New Routes (Stage 2 - Resource & Portfolio Management)

### Portfolio Routes (routes/Portfolios.kt)

```kotlin
fun Routing.portfolios(
    portfolioRepo: IPortfolioRepo,
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) = route("/portfolios") {
    get { /* List all portfolios */ }
    post { /* Create portfolio */ }
    get("/{id}") { /* Get portfolio */ }
    patch("/{id}") { /* Update portfolio */ }
    delete("/{id}") { /* Delete portfolio */ }
    
    get("/{id}/projects") { /* List portfolio projects */ }
    post("/{id}/projects") { /* Create project in portfolio */ }
    patch("/{id}/projects/reorder") { /* Reorder projects */ }
    get("/{id}/resource-load") { /* Cross-project resource load */ }
}
```

### Resource Routes (routes/Resources.kt)

```kotlin
fun Routing.resources(resourceRepo: IResourceRepo) = route("/resources") {
    get { /* List resources */ }
    post { /* Create resource */ }
    patch("/{id}") { /* Update resource */ }
    delete("/{id}") { /* Delete resource */ }
    
    get("/{id}/calendar-overrides") { /* Get calendar overrides */ }
    post("/{id}/calendar-overrides") { /* Add calendar override */ }
    delete("/{id}/calendar-overrides/{date}") { /* Remove override */ }
}
```

### Assignment Routes (routes/Assignments.kt)

```kotlin
fun Route.assignments(
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) {
    route("/assignments") {
        get { /* List assignments */ }
        post { /* Create assignment */ }
        patch("/{id}") { /* Update assignment */ }
        delete("/{id}") { /* Delete assignment */ }
        
        get("/{id}/day-overrides") { /* Get day overrides */ }
        post("/{id}/day-overrides") { /* Set day override */ }
        delete("/{id}/day-overrides/{date}") { /* Remove override */ }
    }
    
    route("/resource-load") {
        get { /* Calculate resource load */ }
        get("/{resourceId}") { /* Get specific resource load */ }
    }
}
```

### Analysis Routes (routes/Analysis.kt)

```kotlin
fun Route.analysis(
    repo: IAtlasProjectTaskRepo,
    calendarService: CacheCalendarProvider,
) = route("/analysis") {
    get("/blocker-chain/{taskId}") { /* Get blocker chain */ }
    get("/available-tasks") { /* Get available tasks */ }
    get("/what-if") { /* What-if start analysis */ }
    get("/what-if-end") { /* What-if end analysis */ }
}
```

### Resource Leveling Routes (routes/Leveling.kt)

```kotlin
fun Route.leveling(
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) {
    route("/leveling") {
        post("/preview") { /* Preview leveling */ }
        post("/apply") { /* Apply leveling */ }
    }
}
```

### Baselines Routes (routes/Baselines.kt)

```kotlin
fun Route.baselines(repo: IAtlasProjectTaskRepo) {
    route("/baselines") {
        get { /* List baselines */ }
        post { /* Create baseline */ }
        get("/{id}") { /* Get baseline */ }
        delete("/{id}") { /* Delete baseline */ }
    }
}
```

### Reorder Tasks Routes (routes/ReorderTasks.kt)

```kotlin
fun Route.reorderTasks(
    taskRepo: IAtlasProjectTaskRepo,
    resourceRepo: IResourceRepo,
    calendarService: CacheCalendarProvider,
) = route("/reorder") {
    patch { /* Reorder tasks */ }
}
```

## Database Tables

### ProjectTasksTable

```kotlin
object ProjectTasksTable : LongIdTable("project_tasks") {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val duration = integer("duration").default(0)
    val status = enumerationByName("status", 20, ProjectTaskStatus::class)
}
```

### TaskSchedulesTable

```kotlin
object TaskSchedulesTable : LongIdTable("task_schedules") {
    val taskId = reference("task_id", ProjectTasksTable)
    val start = date("start")
    val end = date("end")
}
```

### TaskDependenciesTable

```kotlin
object TaskDependenciesTable : Table("task_dependencies") {
    val fromTaskId = reference("from_task_id", ProjectTasksTable)
    val toTaskId = reference("to_task_id", ProjectTasksTable)
    val type = enumerationByName("type", 10, DependencyType::class)
    
    override val primaryKey = PrimaryKey(fromTaskId, toTaskId)
}
```

## Flyway Migrations

Located in `src/main/resources/db/migration/`:

```sql
-- V1__create_project_tasks_table.sql
CREATE TABLE project_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'EMPTY'
);

-- V2__create_task_schedules_table.sql
CREATE TABLE task_schedules (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES project_tasks(id),
    start DATE,
    end DATE
);

-- V3__create_task_dependencies_table.sql
CREATE TABLE task_dependencies (
    from_task_id BIGINT REFERENCES project_tasks(id),
    to_task_id BIGINT REFERENCES project_tasks(id),
    type VARCHAR(10) NOT NULL,
    PRIMARY KEY (from_task_id, to_task_id)
);
```

## Testing

### RoutingTest.kt

```kotlin
class RoutingTest {
    @Test
    fun testGetProjectTasks() = testApplication {
        val response = client.get("/project-tasks")
        assertEquals(HttpStatusCode.OK, response.status)
        val tasks = response.body<List<GanttTaskDto>>()
        assertTrue(tasks.isNotEmpty())
    }
    
    @Test
    fun testCreateTask() = testApplication {
        val response = client.post("/project-tasks/create-in-pool") {
            contentType(ContentType.Application.Json)
            setBody(CreateProjectTaskRequest("Test Task"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val task = response.body<GanttTaskDto>()
        assertEquals("Test Task", task.title)
    }
    
    @Test
    fun testGetCriticalPath() = testApplication {
        val response = client.get("/critical-path")
        assertEquals(HttpStatusCode.OK, response.status)
        val criticalPath = response.body<CriticalPathDto>()
        assertNotNull(criticalPath.criticalTaskIds)
    }
}
```

Run tests:
```bash
./gradlew :atlas-project-backend-ktor-app:test
```

## Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    
    implementation(libs.logback.classic)
    
    implementation(projects.atlasProjectBackendTransport)
    implementation(projects.atlasProjectBackendCommon)
    implementation(projects.atlasProjectBackendMappers)
    implementation(projects.atlasProjectBackendPostgres)
    implementation(projects.atlasProjectBackendCalendarService)
    
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
```

## Running the Application

```bash
# Run with Gradle
./gradlew :atlas-project-backend-ktor-app:run

# Run with Java (after build)
java -jar build/libs/atlas-project-backend-ktor-app.jar

# Run with environment variables
PORT=8080 DATABASE_URL=jdbc:postgresql://localhost:5432/atlas ./gradlew :atlas-project-backend-ktor-app:run
```

## Related Modules

- **transport**: DTOs used in API responses
- **common**: Domain models and business logic
- **mappers**: Domain ↔ Transport mapping
- **postgres**: Repository implementation
- **calendar-service**: Calendar calculations
