package com.khan366kos.atlas.project.backend.repo.inmemory

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.project.PortfolioProject
import com.khan366kos.atlas.project.backend.common.project.Project
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortfolioRepoInMemoryTest {

    private val repo = PortfolioRepoInMemory()

    // --- Portfolio Tests ---

    @Test
    fun `create and retrieve portfolio`() = runTest {
        val portfolio = Portfolio(name = "Test Portfolio", description = "Test Description")
        val created = repo.createPortfolio(portfolio)
        
        assertNotNull(created.id.asString())
        assertEquals("Test Portfolio", created.name)
        assertEquals("Test Description", created.description)

        val retrieved = repo.getPortfolio(created.id.asString())
        assertNotNull(retrieved)
        assertEquals(created.id, retrieved.id)
    }

    @Test
    fun `list all portfolios`() = runTest {
        repo.createPortfolio(Portfolio(name = "Portfolio 1"))
        repo.createPortfolio(Portfolio(name = "Portfolio 2"))
        
        val portfolios = repo.listPortfolios()
        assertEquals(2, portfolios.size)
    }

    @Test
    fun `delete portfolio`() = runTest {
        val portfolio = repo.createPortfolio(Portfolio(name = "To Delete"))
        val deleted = repo.deletePortfolio(portfolio.id.asString())
        
        assertEquals(1, deleted)
        assertNull(repo.getPortfolio(portfolio.id.asString()))
    }

    // --- Project Tests ---

    @Test
    fun `create and retrieve project`() = runTest {
        val project = repo.createProject("Test Project")
        
        assertNotNull(project.id.asString())
        assertEquals("Test Project", project.name.asString())

        val retrieved = repo.getProject(project.id.asString())
        assertNotNull(retrieved)
        assertEquals(project.id, retrieved.id)
    }

    @Test
    fun `list all projects`() = runTest {
        repo.createProject("Project 1")
        repo.createProject("Project 2")
        
        val projects = repo.listAllProjects()
        assertEquals(2, projects.size)
    }

    @Test
    fun `update project name`() = runTest {
        val project = repo.createProject("Original Name")
        repo.updateProject(project.id.asString(), "Updated Name")
        
        val updated = repo.getProject(project.id.asString())
        assertNotNull(updated)
        assertEquals("Updated Name", updated.name.asString())
    }

    @Test
    fun `delete project`() = runTest {
        val project = repo.createProject("To Delete")
        val deleted = repo.deleteProject(project.id.asString())
        
        assertEquals(1, deleted)
        assertNull(repo.getProject(project.id.asString()))
    }

    // --- Portfolio-Project Relationship Tests ---

    @Test
    fun `add project to portfolio`() = runTest {
        val portfolio = repo.createPortfolio(Portfolio(name = "Test Portfolio"))
        val project = repo.createProject("Test Project")
        
        val portfolioProject = repo.addProjectToPortfolio(
            portfolio.id.asString(),
            project.id.asString(),
            ProjectPriority.HIGH
        )
        
        assertNotNull(portfolioProject.id.asString())
        assertEquals(portfolio.id.asString(), portfolioProject.portfolioId.asString())
        assertEquals(project.id.asString(), portfolioProject.projectId.asString())
        assertEquals(ProjectPriority.HIGH, portfolioProject.priority)
    }

    @Test
    fun `list portfolio projects`() = runTest {
        val portfolio = repo.createPortfolio(Portfolio(name = "Test Portfolio"))
        val project1 = repo.createProject("Project 1")
        val project2 = repo.createProject("Project 2")
        
        repo.addProjectToPortfolio(portfolio.id.asString(), project1.id.asString(), ProjectPriority.HIGH)
        repo.addProjectToPortfolio(portfolio.id.asString(), project2.id.asString(), ProjectPriority.LOW)
        
        val portfolioProjects = repo.listPortfolioProjects(portfolio.id.asString())
        assertEquals(2, portfolioProjects.size)
    }

    @Test
    fun `remove project from portfolio`() = runTest {
        val portfolio = repo.createPortfolio(Portfolio(name = "Test Portfolio"))
        val project = repo.createProject("Test Project")
        
        repo.addProjectToPortfolio(portfolio.id.asString(), project.id.asString(), ProjectPriority.MEDIUM)
        val removed = repo.removeProjectFromPortfolio(portfolio.id.asString(), project.id.asString())
        
        assertEquals(1, removed)
        val portfolioProjects = repo.listPortfolioProjects(portfolio.id.asString())
        assertTrue(portfolioProjects.isEmpty())
    }

    @Test
    fun `update project priority in portfolio`() = runTest {
        val portfolio = repo.createPortfolio(Portfolio(name = "Test Portfolio"))
        val project = repo.createProject("Test Project")
        
        repo.addProjectToPortfolio(portfolio.id.asString(), project.id.asString(), ProjectPriority.LOW)
        repo.updateProjectPriority(portfolio.id.asString(), project.id.asString(), ProjectPriority.HIGH)
        
        val portfolioProjects = repo.listPortfolioProjects(portfolio.id.asString())
        assertEquals(1, portfolioProjects.size)
        assertEquals(ProjectPriority.HIGH, portfolioProjects[0].priority)
    }

    @Test
    fun `reorder portfolio projects`() = runTest {
        val portfolio = repo.createPortfolio(Portfolio(name = "Test Portfolio"))
        val project1 = repo.createProject("Project 1")
        val project2 = repo.createProject("Project 2")
        val project3 = repo.createProject("Project 3")
        
        repo.addProjectToPortfolio(portfolio.id.asString(), project1.id.asString(), ProjectPriority.MEDIUM)
        repo.addProjectToPortfolio(portfolio.id.asString(), project2.id.asString(), ProjectPriority.MEDIUM)
        repo.addProjectToPortfolio(portfolio.id.asString(), project3.id.asString(), ProjectPriority.MEDIUM)
        
        // Reorder: project3, project1, project2
        repo.reorderPortfolioProjects(
            portfolio.id.asString(),
            listOf(project3.id.asString(), project1.id.asString(), project2.id.asString())
        )
        
        val portfolioProjects = repo.listPortfolioProjects(portfolio.id.asString())
        assertEquals(3, portfolioProjects.size)
        assertEquals(project3.id.asString(), portfolioProjects[0].projectId.asString())
        assertEquals(project1.id.asString(), portfolioProjects[1].projectId.asString())
        assertEquals(project2.id.asString(), portfolioProjects[2].projectId.asString())
    }

    @Test
    fun `project can belong to multiple portfolios`() = runTest {
        val portfolio1 = repo.createPortfolio(Portfolio(name = "Portfolio 1"))
        val portfolio2 = repo.createPortfolio(Portfolio(name = "Portfolio 2"))
        val project = repo.createProject("Shared Project")
        
        repo.addProjectToPortfolio(portfolio1.id.asString(), project.id.asString(), ProjectPriority.HIGH)
        repo.addProjectToPortfolio(portfolio2.id.asString(), project.id.asString(), ProjectPriority.LOW)
        
        val pp1 = repo.listPortfolioProjects(portfolio1.id.asString())
        val pp2 = repo.listPortfolioProjects(portfolio2.id.asString())
        
        assertEquals(1, pp1.size)
        assertEquals(1, pp2.size)
        assertEquals(project.id.asString(), pp1[0].projectId.asString())
        assertEquals(project.id.asString(), pp2[0].projectId.asString())
        assertEquals(ProjectPriority.HIGH, pp1[0].priority)
        assertEquals(ProjectPriority.LOW, pp2[0].priority)
    }
}
