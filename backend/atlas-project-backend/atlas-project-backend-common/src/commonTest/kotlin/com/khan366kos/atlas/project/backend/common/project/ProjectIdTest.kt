package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProjectIdTest {

    @Test
    fun `test ProjectId creation from String`() {
        val id = ProjectId("test-id-123")
        assertEquals("test-id-123", id.asString())
    }

    @Test
    fun `test ProjectId NONE value`() {
        assertEquals("", ProjectId.NONE.asString())
    }

    @Test
    fun `test ProjectId equality`() {
        val id1 = ProjectId("same-id")
        val id2 = ProjectId("same-id")
        assertEquals(id1, id2)
    }

    @Test
    fun `test ProjectId inequality`() {
        val id1 = ProjectId("id1")
        val id2 = ProjectId("id2")
        assertNotEquals(id1, id2)
    }
}
