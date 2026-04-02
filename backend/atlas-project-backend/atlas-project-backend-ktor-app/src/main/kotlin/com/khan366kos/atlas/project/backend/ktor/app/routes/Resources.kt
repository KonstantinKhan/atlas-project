package com.khan366kos.atlas.project.backend.ktor.app.routes

import com.khan366kos.atlas.project.backend.common.models.resource.Resource
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceCalendarOverride
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceId
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceName
import com.khan366kos.atlas.project.backend.common.models.resource.ResourceType
import com.khan366kos.atlas.project.backend.common.repo.IResourceRepo
import com.khan366kos.atlas.project.backend.mappers.toUpdatableProjectDto
import com.khan366kos.atlas.project.backend.transport.resource.CreateResourceCommandDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceCalendarOverrideDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceCalendarOverrideListDto
import com.khan366kos.atlas.project.backend.transport.resource.ResourceListDto
import com.khan366kos.atlas.project.backend.transport.resource.UpdateResourceCommandDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate

fun Routing.resources(
    resourceRepo: IResourceRepo,
) = route("/resources") {

    get {
        val resources = resourceRepo.listResources()
        call.respond(ResourceListDto(resources = resources.map { it.toUpdatableProjectDto() }))
    }

    post {
        try {
            val request = call.receive<CreateResourceCommandDto>()
            val resource = Resource(
                name = ResourceName(request.name),
                type = ResourceType.valueOf(request.type),
                capacityHoursPerDay = request.capacityHoursPerDay,
            )
            val created = resourceRepo.createResource(resource)
            call.respond(HttpStatusCode.Created, created.toUpdatableProjectDto())
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    patch("/{id}") {
        try {
            val id = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Resource ID parameter is missing")

            val existing = resourceRepo.getResource(id)
                ?: return@patch call.respond(HttpStatusCode.NotFound)

            val request = call.receive<UpdateResourceCommandDto>()
            val updated = existing.copy(
                name = request.name?.let { ResourceName(it) } ?: existing.name,
                type = request.type?.let { ResourceType.valueOf(it) } ?: existing.type,
                capacityHoursPerDay = request.capacityHoursPerDay ?: existing.capacityHoursPerDay,
            )
            call.respond(resourceRepo.updateResource(updated).toUpdatableProjectDto())
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    delete("/{id}") {
        val id = call.parameters["id"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Resource ID parameter is missing")

        resourceRepo.getResource(id)
            ?: return@delete call.respond(HttpStatusCode.NotFound)

        resourceRepo.deleteResource(id)
        call.respond(HttpStatusCode.NoContent)
    }

    get("/{id}/calendar-overrides") {
        val id = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Resource ID parameter is missing")

        resourceRepo.getResource(id)
            ?: return@get call.respond(HttpStatusCode.NotFound)

        val overrides = resourceRepo.getCalendarOverrides(id)
        call.respond(ResourceCalendarOverrideListDto(overrides = overrides.map { it.toUpdatableProjectDto() }))
    }

    post("/{id}/calendar-overrides") {
        val id = call.parameters["id"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Resource ID parameter is missing")

        resourceRepo.getResource(id)
            ?: return@post call.respond(HttpStatusCode.NotFound)

        val request = call.receive<ResourceCalendarOverrideDto>()
        val override = ResourceCalendarOverride(
            resourceId = ResourceId(id),
            date = LocalDate.parse(request.date),
            availableHours = request.availableHours,
        )
        resourceRepo.setCalendarOverride(override)
        call.respond(HttpStatusCode.Created, override.toUpdatableProjectDto())
    }

    delete("/{id}/calendar-overrides/{date}") {
        val id = call.parameters["id"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Resource ID parameter is missing")
        val date = call.parameters["date"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Date parameter is missing")

        resourceRepo.getResource(id)
            ?: return@delete call.respond(HttpStatusCode.NotFound)

        resourceRepo.deleteCalendarOverride(id, LocalDate.parse(date))
        call.respond(HttpStatusCode.NoContent)
    }
}
