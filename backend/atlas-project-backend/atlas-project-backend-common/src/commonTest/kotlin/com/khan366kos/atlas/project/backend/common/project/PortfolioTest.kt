package com.khan366kos.atlas.project.backend.common.project

import com.khan366kos.atlas.project.backend.common.models.portfolio.ProjectPortfolioId
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectPortfolioProjectTest {

    @Test
    fun `test PortfolioProject creation with default values`() {
        val portfolioProject = PortfolioProject()
        assertEquals(PortfolioProjectId.NONE, portfolioProject.id)
        assertEquals(ProjectPortfolioId.NONE, portfolioProject.projectPortfolioId)
        assertEquals(ProjectId.NONE, portfolioProject.projectId)
        assertEquals(ProjectPriority.MEDIUM, portfolioProject.priority)
    }

    @Test
    fun `test PortfolioProject creation with custom values`() {
        val portfolioProject = PortfolioProject(
            id = PortfolioProjectId("pp-id-1"),
            projectPortfolioId = ProjectPortfolioId("portfolio-1"),
            projectId = ProjectId("project-1"),
            priority = ProjectPriority.HIGH,
        )
        assertEquals("pp-id-1", portfolioProject.id.asString())
        assertEquals("portfolio-1", portfolioProject.projectPortfolioId.asString())
        assertEquals("project-1", portfolioProject.projectId.asString())
        assertEquals(ProjectPriority.HIGH, portfolioProject.priority)
    }

    @Test
    fun `test PortfolioProject NONE value`() {
        val none = PortfolioProject.NONE
        assertEquals(PortfolioProjectId.NONE, none.id)
        assertEquals(ProjectPortfolioId.NONE, none.projectPortfolioId)
        assertEquals(ProjectId.NONE, none.projectId)
        assertEquals(ProjectPriority.MEDIUM, none.priority)
    }

    @Test
    fun `test PortfolioProject copy`() {
        val original = PortfolioProject(
            id = PortfolioProjectId("pp-id-1"),
            projectPortfolioId = ProjectPortfolioId("portfolio-1"),
            projectId = ProjectId("project-1"),
            priority = ProjectPriority.MEDIUM,
        )
        val updated = original.copy(priority = ProjectPriority.HIGH)
        assertEquals(ProjectPriority.HIGH, updated.priority)
        assertEquals(original.id, updated.id)
        assertEquals(original.projectPortfolioId, updated.projectPortfolioId)
        assertEquals(original.projectId, updated.projectId)
    }
}
