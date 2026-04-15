package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import kotlin.test.Test
import kotlin.test.assertEquals

class PortfolioTest {

    @Test
    fun `test PortfolioProject creation with default values`() {
        val portfolio = Portfolio()
        assertEquals(PortfolioProjectId.NONE, portfolio.id)
        assertEquals(PortfolioId.NONE, portfolio.portfolioId)
        assertEquals(ProjectId.NONE, portfolio.projectId)
        assertEquals(ProjectPriority.MEDIUM, portfolio.priority)
    }

    @Test
    fun `test PortfolioProject creation with custom values`() {
        val portfolio = Portfolio(
            id = PortfolioProjectId("pp-id-1"),
            portfolioId = PortfolioId("portfolio-1"),
            projectId = ProjectId("project-1"),
            priority = ProjectPriority.HIGH,
        )
        assertEquals("pp-id-1", portfolio.id.asString())
        assertEquals("portfolio-1", portfolio.portfolioId.asString())
        assertEquals("project-1", portfolio.projectId.asString())
        assertEquals(ProjectPriority.HIGH, portfolio.priority)
    }

    @Test
    fun `test PortfolioProject NONE value`() {
        val none = Portfolio.NONE
        assertEquals(PortfolioProjectId.NONE, none.id)
        assertEquals(PortfolioId.NONE, none.portfolioId)
        assertEquals(ProjectId.NONE, none.projectId)
        assertEquals(ProjectPriority.MEDIUM, none.priority)
    }

    @Test
    fun `test PortfolioProject copy`() {
        val original = Portfolio(
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
