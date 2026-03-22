package com.khan366kos

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.projectPlan.ProjectPlan
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.task.ProjectTask
import com.khan366kos.atlas.project.backend.common.models.task.simple.Description
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.models.task.simple.Title
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.timelineCalendar.TimelineCalendar
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.ktor.app.plugins.configureRouting
import com.khan366kos.atlas.project.backend.repo.inmemory.PortfolioRepoInMemory
import com.khan366kos.atlas.project.backend.repo.inmemory.ResourceRepoInMemory
import com.khan366kos.config.AppConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutingTest {

    private val testCalendar = TimelineCalendar(
        workingWeekDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        ),
        weekendWeekDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    )

    private fun createTestRepo(
        tasks: MutableMap<String, ProjectTask> = mutableMapOf(),
        schedules: MutableMap<TaskScheduleId, TaskSchedule> = mutableMapOf(),
        dependencies: MutableSet<TaskDependency> = mutableSetOf(),
    ): IAtlasProjectTaskRepo = object : IAtlasProjectTaskRepo {

        override suspend fun timelineCalendar(): TimelineCalendar = testCalendar

        override suspend fun projectPlan(planId: String): ProjectPlan = ProjectPlan(
            tasks = tasks.map { TaskId(it.key) to it.value }.toMap().toMutableMap(),
            schedules = schedules,
            dependencies = dependencies,
        )

        override suspend fun updateSchedule(schedule: TaskSchedule): Int {
            schedules[schedule.id] = schedule
            return 1
        }

        override suspend fun tasks(): List<ProjectTask> = tasks.values.toList()

        override suspend fun getTask(id: String): ProjectTask? = tasks[id]

        override suspend fun createTask(planId: String, task: ProjectTask): ProjectTask {
            tasks[task.id.value] = task
            return task
        }

        override suspend fun createTaskWithoutSchedule(planId: String, task: ProjectTask): ProjectTask {
            tasks[task.id.value] = task
            return task
        }

        override suspend fun updateTask(task: ProjectTask): ProjectTask {
            tasks[task.id.value] = task
            return task
        }

        override suspend fun addDependency(planId: String, predecessorId: String, successorId: String, type: String, lagDays: Int): Int {
            dependencies.add(
                TaskDependency(
                    predecessor = TaskId(predecessorId),
                    successor = TaskId(successorId),
                    type = DependencyType.valueOf(type),
                    lag = Duration(lagDays)
                )
            )
            return 1
        }

        override suspend fun updateDependencyLag(predecessorId: String, successorId: String, lag: Int): Int = 1

        override suspend fun updateDependency(predecessorId: String, successorId: String, type: String, lagDays: Int): Int = 1

        override suspend fun deleteDependency(predecessorId: String, successorId: String): Int {
            dependencies.removeAll { it.predecessor == TaskId(predecessorId) && it.successor == TaskId(successorId) }
            return 1
        }

        override suspend fun deleteTask(id: String): Int {
            tasks.remove(id)
            return 1
        }

        override suspend fun reorderTasks(orderedIds: List<String>) {}
    }

    private fun testApp(
        repo: IAtlasProjectTaskRepo = createTestRepo(),
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        application {
            val config = AppConfig(repo, ResourceRepoInMemory(), PortfolioRepoInMemory())
            configureSerialization()
            configureHTTP()
            configureStatusPages()
            configureRoutingOld(config)
            configureRouting(config)
        }
        block()
    }

    @Test
    fun getProjectPlan_returnsOk() = testApp {
        val response = client.get("/projects/1/plan")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun postCreateTaskInPool_returnsCreated() = testApp {
        val response = client.post("/projects/1/project-tasks/create-in-pool") {
            contentType(ContentType.Application.Json)
            setBody("""{"title": "Test Task"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Test Task"))
    }

    @Test
    fun patchProjectTask_updatesTitle() {
        val taskId = "test-task-1"
        val tasks = mutableMapOf(
            taskId to ProjectTask(
                id = TaskId(taskId),
                title = Title("Original"),
                description = Description("desc"),
                duration = Duration(3),
            )
        )
        val repo = createTestRepo(tasks = tasks)

        testApp(repo) {
            val response = client.patch("/projects/1/project-tasks/$taskId") {
                contentType(ContentType.Application.Json)
                setBody("""{"title": "Updated Title"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("Updated Title"))
        }
    }

    @Test
    fun postChangeStart_updatesSchedule() {
        val taskId = "task-a"
        val tasks = mutableMapOf(
            taskId to ProjectTask(id = TaskId(taskId), duration = Duration(3))
        )
        val schedules = mutableMapOf(
            TaskScheduleId(taskId) to TaskSchedule(
                id = TaskScheduleId(taskId),
                start = ProjectDate.Set(LocalDate(2025, 3, 3)),
                end = ProjectDate.Set(LocalDate(2025, 3, 5)),
            )
        )
        val repo = createTestRepo(tasks = tasks, schedules = schedules)

        testApp(repo) {
            val response = client.post("/projects/1/change-start") {
                contentType(ContentType.Application.Json)
                setBody("""{"planId":"1","taskId":"$taskId","newPlannedStart":"2025-03-10"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun postChangeEnd_updatesSchedule() {
        val taskId = "task-a"
        val tasks = mutableMapOf(
            taskId to ProjectTask(id = TaskId(taskId), duration = Duration(3))
        )
        val schedules = mutableMapOf(
            TaskScheduleId(taskId) to TaskSchedule(
                id = TaskScheduleId(taskId),
                start = ProjectDate.Set(LocalDate(2025, 3, 3)),
                end = ProjectDate.Set(LocalDate(2025, 3, 5)),
            )
        )
        val repo = createTestRepo(tasks = tasks, schedules = schedules)

        testApp(repo) {
            val response = client.post("/projects/1/change-end") {
                contentType(ContentType.Application.Json)
                setBody("""{"planId":"1","taskId":"$taskId","newPlannedEnd":"2025-03-07"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun postDependencies_createsDependency() {
        val taskA = "task-a"
        val taskB = "task-b"
        val tasks = mutableMapOf(
            taskA to ProjectTask(id = TaskId(taskA), duration = Duration(3)),
            taskB to ProjectTask(id = TaskId(taskB), duration = Duration(2)),
        )
        val schedules = mutableMapOf(
            TaskScheduleId(taskA) to TaskSchedule(id = TaskScheduleId(taskA), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 5))),
            TaskScheduleId(taskB) to TaskSchedule(id = TaskScheduleId(taskB), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
        )
        val repo = createTestRepo(tasks = tasks, schedules = schedules)

        testApp(repo) {
            val response = client.post("/projects/1/dependencies") {
                contentType(ContentType.Application.Json)
                setBody("""{"planId":"1","fromTaskId":"$taskA","toTaskId":"$taskB","type":"FS"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun postDependencies_circularDependency_returnsError() {
        val taskA = "task-a"
        val taskB = "task-b"
        val tasks = mutableMapOf(
            taskA to ProjectTask(id = TaskId(taskA), duration = Duration(1)),
            taskB to ProjectTask(id = TaskId(taskB), duration = Duration(1)),
        )
        val schedules = mutableMapOf(
            TaskScheduleId(taskA) to TaskSchedule(id = TaskScheduleId(taskA), start = ProjectDate.Set(LocalDate(2025, 3, 3)), end = ProjectDate.Set(LocalDate(2025, 3, 3))),
            TaskScheduleId(taskB) to TaskSchedule(id = TaskScheduleId(taskB), start = ProjectDate.Set(LocalDate(2025, 3, 4)), end = ProjectDate.Set(LocalDate(2025, 3, 4))),
        )
        val deps = mutableSetOf(
            TaskDependency(predecessor = TaskId(taskA), successor = TaskId(taskB), type = DependencyType.FS)
        )
        val repo = createTestRepo(tasks = tasks, schedules = schedules, dependencies = deps)

        testApp(repo) {
            val response = client.post("/projects/1/dependencies") {
                contentType(ContentType.Application.Json)
                setBody("""{"planId":"1","fromTaskId":"$taskB","toTaskId":"$taskA","type":"FS"}""")
            }
            // Circular dependency should trigger error via StatusPages
            assertTrue(response.status == HttpStatusCode.InternalServerError || response.status == HttpStatusCode.BadRequest)
        }
    }
}
