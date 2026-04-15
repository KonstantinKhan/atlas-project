package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.project.ProjectName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProjectNameTest {

    @Test
    fun `test ProjectName creation from String`() {
        val name = ProjectName("Test Project")
        assertEquals("Test Project", name.asString())
    }

    @Test
    fun `test ProjectName NONE value`() {
        assertEquals("", ProjectName.NONE.asString())
    }

    @Test
    fun `test ProjectName equality`() {
        val name1 = ProjectName("Same Name")
        val name2 = ProjectName("Same Name")
        assertEquals(name1, name2)
    }

    @Test
    fun `test ProjectName inequality`() {
        val name1 = ProjectName("Name 1")
        val name2 = ProjectName("Name 2")
        assertNotEquals(name1, name2)
    }
}
