package com.khan366kos

import com.khan366kos.atlas.project.backend.common.models.ProjectDate
import com.khan366kos.atlas.project.backend.common.models.simple.Duration
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskSchedule
import com.khan366kos.atlas.project.backend.common.models.taskSchedule.TaskScheduleId
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import com.khan366kos.atlas.project.backend.common.repo.IAtlasProjectTaskRepo
import com.khan366kos.atlas.project.backend.mappers.toDto
import com.khan366kos.atlas.project.backend.mappers.toGanttDto
import com.khan366kos.atlas.project.backend.mappers.toTransport
import com.khan366kos.atlas.project.backend.mappers.toDomain
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskStartDateCommandDto
import com.khan366kos.atlas.project.backend.transport.commands.CreateDependencyCommandDto
import com.khan366kos.atlas.project.backend.transport.CreateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.UpdateProjectTaskRequest
import com.khan366kos.atlas.project.backend.transport.commands.ChangeTaskEndDateCommandDto
import com.khan366kos.config.AppConfig
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting(config: AppConfig) {
    routing {
        get("/work-calendar") {
            val timelineCalendar = config.calendarService.current()
            call.respond(timelineCalendar.toTransport())
        }
        get("/project-plan") {
            val plan = config.repo.projectPlan()
            call.respond(plan.toGanttDto())
        }

        post("/change-start") {
            val request = call.receive<ChangeTaskStartDateCommandDto>()
            val plan = config.repo.projectPlan()
            val delta = plan.changeTaskStartDate(
                taskId = TaskId(request.taskId),
                newStart = request.newPlannedStart,
                calendar = config.calendarService.current()
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            call.respond(delta.toDto())
        }

        post("/change-end") {
            val request = call.receive<ChangeTaskEndDateCommandDto>()
            val plan = config.repo.projectPlan()
            val delta = plan.changeTaskEndDate(
                taskId = TaskId(request.taskId),
                newEnd = request.newPlannedEnd,
                calendar = config.calendarService.current()
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            call.respond(delta.toDto())
        }

        post("/dependencies") {
            val request = call.receive<CreateDependencyCommandDto>()
            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()
            
            println("[Routing] Creating dependency: from=${request.fromTaskId}, to=${request.toTaskId}, lag=${request.lagDays}")
            
            val delta = plan.addDependency(
                predecessorId = TaskId(request.fromTaskId),
                successorId = TaskId(request.toTaskId),
                type = request.type.toDomain(),
                lagDays = request.lagDays,
                calendar = calendar
            )
            
            println("[Routing] Dependency created, delta schedules: ${delta.updatedSchedule.size}")
            delta.updatedSchedule.forEach {
                println("[Routing]   Task ${it.id.value}: start=${it.start}, end=${it.end}")
            }
            
            // Persist the dependency and updated schedules
            config.repo.addDependency(
                predecessorId = request.fromTaskId,
                successorId = request.toTaskId,
                type = request.type.name,
                lagDays = request.lagDays ?: 0
            )
            delta.updatedSchedule.forEach {
                plan.schedules()[it.id] = it
                config.repo.updateSchedule(it)
            }
            
            // Return the updated plan
            call.respond(plan.toGanttDto())
        }

        post("/dependencies/recalculate") {
            val plan = config.repo.projectPlan()
            val calendar = config.calendarService.current()
            
            println("[Routing] Recalculating all dependencies...")
            
            // Get all tasks without dependencies as starting points
            val allTasks = plan.tasks()
            val allDeps = plan.dependencies()
            val tasksWithPredecessors = allDeps.map { it.successor }.toSet()
            val rootTasks = allTasks.filter { it.id !in tasksWithPredecessors }
            
            println("[Routing] Found ${rootTasks.size} root tasks")
            
            // BFS through dependency graph
            val visited = mutableSetOf<TaskId>()
            val queue = ArrayDeque<TaskId>()
            
            // Add all root tasks to queue
            rootTasks.forEach { task ->
                queue.add(task.id)
                visited.add(task.id)
            }
            
            val updatedSchedules = mutableListOf<TaskSchedule>()
            
            while (queue.isNotEmpty()) {
                val currentId = queue.removeFirst()
                
                // Find all tasks that depend on current
                val dependents = allDeps.filter { it.predecessor == currentId }
                
                for (dep in dependents) {
                    val successorId = dep.successor
                    val successorTask = allTasks.find { it.id == successorId } ?: continue
                    
                    // Calculate constrained start based on ALL predecessors of successor
                    val allPreds = allDeps.filter { it.successor == successorId }
                    val constrainedStart = allPreds.mapNotNull { pd ->
                        val predSched = plan.schedules()[TaskScheduleId(pd.predecessor.value)]
                            ?.takeIf { it.start is ProjectDate.Set && it.end is ProjectDate.Set }
                            ?: return@mapNotNull null
                        val predEnd = (predSched.end as ProjectDate.Set).date
                        val lag = pd.lag.asInt()  // Используем lag из pd, а не dep
                        calendar.addWorkingDays(predEnd, Duration(lag + 1))
                    }.maxOrNull()
                    
                    if (constrainedStart != null) {
                        val newEnd = calendar.addWorkingDays(constrainedStart, successorTask.duration)
                        val updatedSched = TaskSchedule(
                            id = TaskScheduleId(successorId.value),
                            start = ProjectDate.Set(constrainedStart),
                            end = ProjectDate.Set(newEnd),
                        )
                        plan.schedules()[updatedSched.id] = updatedSched
                        updatedSchedules.add(updatedSched)
                        
                        if (successorId !in visited) {
                            visited.add(successorId)
                            queue.add(successorId)
                        }
                    }
                }
            }
            
            println("[Routing] Recalculated ${updatedSchedules.size} schedules")
            updatedSchedules.forEach {
                println("[Routing]   Task ${it.id.value}: start=${it.start}, end=${it.end}")
                config.repo.updateSchedule(it)
            }
            
            // Return the updated plan
            val updatedPlan = config.repo.projectPlan()
            call.respond(updatedPlan.toGanttDto())
        }

        post("/project-tasks") {
            try {
                val request = call.receive<CreateProjectTaskRequest>()
                val created = config.repo.createTask(request.toModel())

                val calendar = config.calendarService.current()
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val startDate = calendar.currentOrNextWorkingDay(today)
                val duration = created.duration.let {
                    if (it == Duration.NONE || it.asInt() <= 0) Duration(1) else it
                }
                val endDate = calendar.addWorkingDays(startDate, duration)

                config.repo.updateSchedule(
                    TaskSchedule(
                        id = TaskScheduleId(created.id.value),
                        start = ProjectDate.Set(startDate),
                        end = ProjectDate.Set(endDate),
                    )
                )
                call.respond(HttpStatusCode.Created, created.toTransport())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        patch("/project-tasks/{id}") {
            try {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Task ID parameter is missing")

                if (id.isBlank()) {
                    return@patch call.respond(HttpStatusCode.BadRequest, "Task ID cannot be empty")
                }

                val request = call.receive<UpdateProjectTaskRequest>()

                val existing = config.repo.getTask(id)
                    ?: return@patch call.respond(HttpStatusCode.NotFound)

                val updated = existing.applyUpdate(request)
                call.respond(config.repo.updateTask(updated).toTransport())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
