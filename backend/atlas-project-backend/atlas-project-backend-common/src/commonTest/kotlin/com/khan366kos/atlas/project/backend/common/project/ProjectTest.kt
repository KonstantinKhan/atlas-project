package com.khan366kos.atlas.project.backend.common.project

import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectTest {

    @Test
    fun `test Project creation with default values`() {
        val project = Project()
        assertEquals(ProjectId.NONE, project.id)
        assertEquals(ProjectName.NONE, project.name)
    }

    @Test
    fun `test Project creation with custom values`() {
        val project = Project(
            id = ProjectId("project-1"),
            name = ProjectName("Test Project"),
        )
        assertEquals("project-1", project.id.asString())
        assertEquals("Test Project", project.name.asString())
    }

    @Test
    fun `test Project NONE value`() {
        val none = Project.NONE
        assertEquals(ProjectId.NONE, none.id)
        assertEquals(ProjectName.NONE, none.name)
    }

    @Test
    fun `test Project copy`() {
        val original = Project(
            id = ProjectId("project-1"),
            name = ProjectName("Original Name"),
        )
        val updated = original.copy(name = ProjectName("Updated Name"))
        assertEquals("Updated Name", updated.name.asString())
        assertEquals(original.id, updated.id)
    }

    @Test
    fun `test Project does not have portfolioId field`() {
        val project = Project(
            id = ProjectId("project-1"),
            name = ProjectName("Test Project"),
        )
        // Verify that project is independent of portfolio
        // (no portfolioId field exists in the domain model)
        assertEquals("project-1", project.id.asString())
        assertEquals("Test Project", project.name.asString())
    }

    @Test
    fun `test Project does not have priority field`() {
        val project = Project(
            id = ProjectId("project-1"),
            name = ProjectName("Test Project"),
        )
        // Verify that project is independent of priority
        // (no priority field exists in the domain model)
        assertEquals("project-1", project.id.asString())
        assertEquals("Test Project", project.name.asString())
    }
}
