package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import kotlin.test.Test
import kotlin.test.assertEquals

class PortfolioProjectTest {

    @Test
    fun `test PortfolioProject creation with default values`() {
        val portfolioProject = PortfolioProject()
        assertEquals(PortfolioProjectId.NONE, portfolioProject.id)
        assertEquals(PortfolioId.NONE, portfolioProject.portfolioId)
        assertEquals(ProjectId.NONE, portfolioProject.projectId)
        assertEquals(ProjectPriority.MEDIUM, portfolioProject.priority)
    }

    @Test
    fun `test PortfolioProject creation with custom values`() {
        val portfolioProject = PortfolioProject(
            id = PortfolioProjectId("pp-id-1"),
            portfolioId = PortfolioId("portfolio-1"),
            projectId = ProjectId("project-1"),
            priority = ProjectPriority.HIGH,
        )
        assertEquals("pp-id-1", portfolioProject.id.asString())
        assertEquals("portfolio-1", portfolioProject.portfolioId.asString())
        assertEquals("project-1", portfolioProject.projectId.asString())
        assertEquals(ProjectPriority.HIGH, portfolioProject.priority)
    }

    @Test
    fun `test PortfolioProject NONE value`() {
        val none = PortfolioProject.NONE
        assertEquals(PortfolioProjectId.NONE, none.id)
        assertEquals(PortfolioId.NONE, none.portfolioId)
        assertEquals(ProjectId.NONE, none.projectId)
        assertEquals(ProjectPriority.MEDIUM, none.priority)
    }

    @Test
    fun `test PortfolioProject copy`() {
        val original = PortfolioProject(
            id = PortfolioProjectId("pp-id-1"),
            portfolioId = PortfolioId("portfolio-1"),
            projectId = ProjectId("project-1"),
            priority = ProjectPriority.MEDIUM,
        )
        val updated = original.copy(priority = ProjectPriority.HIGH)
        assertEquals(ProjectPriority.HIGH, updated.priority)
        assertEquals(original.id, updated.id)
        assertEquals(original.portfolioId, updated.portfolioId)
        assertEquals(original.projectId, updated.projectId)
    }
}
