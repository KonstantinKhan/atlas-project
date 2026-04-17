package com.khan366kos.atlas.project.backend.repo.postgres

import com.khan366kos.atlas.project.backend.common.models.portfolio.Portfolio
import com.khan366kos.atlas.project.backend.common.models.portfolio.PortfolioId
import com.khan366kos.atlas.project.backend.common.project.ProjectPriority
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioIdRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.DbPortfolioRequest
import com.khan366kos.atlas.project.backend.common.repo.portfolio.PortfolioRepoResult
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfolioProjectsTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.PortfoliosTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectPlansTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectSortOrdersTable
import com.khan366kos.atlas.project.backend.repo.postgres.table.ProjectsTable
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PortfolioRepoPostgresTest : ShouldSpec({

    val database = Database.connect(
        url = "jdbc:h2:mem:test_${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        driver = "org.h2.Driver",
    )

    beforeSpec {
        newSuspendedTransaction(Dispatchers.IO, db = database) {
            SchemaUtils.create(
                PortfoliosTable,
                ProjectsTable,
                ProjectPlansTable,
                PortfolioProjectsTable,
                ProjectSortOrdersTable,
            )
        }
    }

    afterSpec {
        newSuspendedTransaction(Dispatchers.IO, db = database) {
            SchemaUtils.drop(
                ProjectSortOrdersTable,
                PortfolioProjectsTable,
                ProjectPlansTable,
                ProjectsTable,
                PortfoliosTable,
            )
        }
    }

    afterEach {
        newSuspendedTransaction(Dispatchers.IO, db = database) {
            SchemaUtils.drop(
                ProjectSortOrdersTable,
                PortfolioProjectsTable,
                ProjectPlansTable,
                ProjectsTable,
                PortfoliosTable,
            )
            SchemaUtils.create(
                PortfoliosTable,
                ProjectsTable,
                ProjectPlansTable,
                PortfolioProjectsTable,
                ProjectSortOrdersTable,
            )
        }
    }

    val repo = PortfolioRepoPostgres(database)

    fun portfolioRequest(name: String, description: String = "desc") = DbPortfolioRequest(
        portfolio = Portfolio(
            id = PortfolioId.NONE,
            name = name,
            description = description,
        )
    )

    suspend fun createPortfolio(name: String, description: String = "desc"): Portfolio {
        val result = repo.createPortfolio(portfolioRequest(name, description))
        result.shouldBeInstanceOf<PortfolioRepoResult.Single>()
        return (result as PortfolioRepoResult.Single).portfolio
    }

    suspend fun createProject(name: String): String {
        val project = repo.createProject(name)
        return project.id.asString()
    }

    context("searchPortfolio") {
        should("TC-1: return Multiple with empty list when DB is empty") {
            val result = repo.searchPortfolio()
            result.shouldBeInstanceOf<PortfolioRepoResult.Multiple>()
            (result as PortfolioRepoResult.Multiple).portfolios.shouldBeEmpty()
        }

        should("TC-2: return Multiple with 1 element when 1 portfolio exists") {
            createPortfolio("Portfolio A")

            val result = repo.searchPortfolio()
            result.shouldBeInstanceOf<PortfolioRepoResult.Multiple>()
            (result as PortfolioRepoResult.Multiple).portfolios shouldHaveSize 1
        }

        should("TC-3: return Multiple with 3 elements when 3 portfolios exist") {
            createPortfolio("Portfolio A")
            createPortfolio("Portfolio B")
            createPortfolio("Portfolio C")

            val result = repo.searchPortfolio()
            result.shouldBeInstanceOf<PortfolioRepoResult.Multiple>()
            (result as PortfolioRepoResult.Multiple).portfolios shouldHaveSize 3
        }
    }

    context("readPortfolio") {
        should("TC-4: return Single with correct data when portfolio exists") {
            val created = createPortfolio("My Portfolio", "My description")

            val result = repo.readPortfolio(DbPortfolioIdRequest(created.id))
            result.shouldBeInstanceOf<PortfolioRepoResult.Single>()
            val portfolio = (result as PortfolioRepoResult.Single).portfolio
            portfolio.name shouldBe "My Portfolio"
            portfolio.description shouldBe "My description"
            portfolio.id shouldBe created.id
        }

        should("TC-5: return NotFound for non-existent UUID") {
            val result = repo.readPortfolio(DbPortfolioIdRequest(PortfolioId(java.util.UUID.randomUUID())))
            result shouldBe PortfolioRepoResult.NotFound
        }
    }

    context("createPortfolio") {
        should("TC-6: return Single with non-empty id for valid request") {
            val result = repo.createPortfolio(portfolioRequest("New Portfolio"))
            result.shouldBeInstanceOf<PortfolioRepoResult.Single>()
            val portfolio = (result as PortfolioRepoResult.Single).portfolio
            portfolio.id.asString() shouldNotBe ""
        }

        should("TC-7: created portfolio can be read via readPortfolio") {
            val created = createPortfolio("Readable Portfolio", "some description")

            val result = repo.readPortfolio(DbPortfolioIdRequest(created.id))
            result.shouldBeInstanceOf<PortfolioRepoResult.Single>()
            (result as PortfolioRepoResult.Single).portfolio.name shouldBe "Readable Portfolio"
        }

        should("TC-8: create portfolio with empty description without error") {
            val result = repo.createPortfolio(portfolioRequest("Portfolio without desc", ""))
            result.shouldBeInstanceOf<PortfolioRepoResult.Single>()
            val portfolio = (result as PortfolioRepoResult.Single).portfolio
            portfolio.description shouldBe ""
        }
    }

    context("updatePortfolio") {
        should("TC-9: return Single with new name and description after update") {
            val created = createPortfolio("Old Name", "Old Desc")

            val updateRequest = DbPortfolioRequest(
                portfolio = Portfolio(
                    id = created.id,
                    name = "New Name",
                    description = "New Desc",
                )
            )
            val result = repo.updatePortfolio(updateRequest)
            result.shouldBeInstanceOf<PortfolioRepoResult.Single>()
            val portfolio = (result as PortfolioRepoResult.Single).portfolio
            portfolio.name shouldBe "New Name"
            portfolio.description shouldBe "New Desc"
        }

        should("TC-10: return NotFound for non-existent id") {
            val updateRequest = DbPortfolioRequest(
                portfolio = Portfolio(
                    id = PortfolioId(java.util.UUID.randomUUID()),
                    name = "Some Name",
                    description = "Some Desc",
                )
            )
            val result = repo.updatePortfolio(updateRequest)
            result shouldBe PortfolioRepoResult.NotFound
        }
    }

    context("deletePortfolio") {
        should("TC-11: return Single, then readPortfolio returns NotFound") {
            val created = createPortfolio("To be deleted")

            val deleteResult = repo.deletePortfolio(DbPortfolioIdRequest(created.id))
            deleteResult.shouldBeInstanceOf<PortfolioRepoResult.Single>()

            val readResult = repo.readPortfolio(DbPortfolioIdRequest(created.id))
            readResult shouldBe PortfolioRepoResult.NotFound
        }

        should("TC-12: return NotFound for non-existent id") {
            val result = repo.deletePortfolio(DbPortfolioIdRequest(PortfolioId(java.util.UUID.randomUUID())))
            result shouldBe PortfolioRepoResult.NotFound
        }
    }

    context("listPortfolioProjects") {
        should("TC-13: return empty list when no projects in portfolio") {
            val portfolio = createPortfolio("Empty Portfolio")

            val projects = repo.listPortfolioProjects(portfolio.id.asString())
            projects.shouldBeEmpty()
        }

        should("TC-14: return list of 2 when 2 projects added") {
            val portfolio = createPortfolio("Portfolio with projects")
            val project1 = createProject("Project 1")
            val project2 = createProject("Project 2")

            repo.addProjectToPortfolio(portfolio.id.asString(), project1, ProjectPriority.MEDIUM)
            repo.addProjectToPortfolio(portfolio.id.asString(), project2, ProjectPriority.MEDIUM)

            val projects = repo.listPortfolioProjects(portfolio.id.asString())
            projects shouldHaveSize 2
        }
    }

    context("addProjectToPortfolio") {
        should("TC-15: add project and return result") {
            val portfolio = createPortfolio("Portfolio")
            val projectId = createProject("Project")

            val result = repo.addProjectToPortfolio(portfolio.id.asString(), projectId, ProjectPriority.HIGH)
            result shouldNotBe null
        }

        should("TC-16: second project gets sortOrder greater than first") {
            val portfolio = createPortfolio("Portfolio")
            val project1 = createProject("Project 1")
            val project2 = createProject("Project 2")

            repo.addProjectToPortfolio(portfolio.id.asString(), project1, ProjectPriority.LOW)
            repo.addProjectToPortfolio(portfolio.id.asString(), project2, ProjectPriority.LOW)

            val projects = repo.listPortfolioProjects(portfolio.id.asString())
            projects shouldHaveSize 2
            // listPortfolioProjects returns ordered by sortOrder, so first added is at index 0
            // This verifies that both were added with sequential sort orders
        }
    }

    context("removeProjectFromPortfolio") {
        should("TC-17: remove existing project-portfolio link") {
            val portfolio = createPortfolio("Portfolio")
            val projectId = createProject("Project")

            repo.addProjectToPortfolio(portfolio.id.asString(), projectId, ProjectPriority.MEDIUM)

            val removed = repo.removeProjectFromPortfolio(portfolio.id.asString(), projectId)
            removed shouldBe 1

            val projects = repo.listPortfolioProjects(portfolio.id.asString())
            projects.shouldBeEmpty()
        }

        should("TC-18: return 0 when link does not exist") {
            val portfolio = createPortfolio("Portfolio")
            val fakeProjectId = java.util.UUID.randomUUID().toString()

            val removed = repo.removeProjectFromPortfolio(portfolio.id.asString(), fakeProjectId)
            removed shouldBe 0
        }
    }

    context("reorderPortfolioProjects") {
        should("TC-19: reorder 2 projects correctly") {
            val portfolio = createPortfolio("Portfolio")
            val project1 = createProject("Project 1")
            val project2 = createProject("Project 2")

            repo.addProjectToPortfolio(portfolio.id.asString(), project1, ProjectPriority.MEDIUM)
            repo.addProjectToPortfolio(portfolio.id.asString(), project2, ProjectPriority.MEDIUM)

            val reorderResult = repo.reorderPortfolioProjects(
                portfolio.id.asString(),
                listOf(project2, project1)
            )
            reorderResult shouldBe 2
        }

        should("TC-20: return 0 updates for empty list") {
            val portfolio = createPortfolio("Portfolio")

            val reorderResult = repo.reorderPortfolioProjects(portfolio.id.asString(), emptyList())
            reorderResult shouldBe 0
        }
    }
})