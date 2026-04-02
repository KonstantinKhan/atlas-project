package com.khan366kos.atlas.project.backend.common.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProjectPortfolioProjectIdTest {

    @Test
    fun `test PortfolioProjectId creation from String`() {
        val id = PortfolioProjectId("test-id-123")
        assertEquals("test-id-123", id.asString())
    }

    @Test
    fun `test PortfolioProjectId NONE value`() {
        assertEquals("", PortfolioProjectId.NONE.asString())
    }

    @Test
    fun `test PortfolioProjectId equality`() {
        val id1 = PortfolioProjectId("same-id")
        val id2 = PortfolioProjectId("same-id")
        assertEquals(id1, id2)
    }

    @Test
    fun `test PortfolioProjectId inequality`() {
        val id1 = PortfolioProjectId("id1")
        val id2 = PortfolioProjectId("id2")
        assertNotEquals(id1, id2)
    }
}
