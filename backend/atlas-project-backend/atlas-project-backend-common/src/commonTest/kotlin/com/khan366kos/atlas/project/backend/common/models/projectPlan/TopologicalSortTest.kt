package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.enums.DependencyType
import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TopologicalSortTest {

    private fun id(s: String) = TaskId(s)

    private fun dep(from: String, to: String) = TaskDependency(
        predecessor = id(from),
        successor = id(to),
        type = DependencyType.FS,
    )

    @Test
    fun linearChain() {
        val result = topologicalSort(
            taskIds = setOf(id("A"), id("B"), id("C")),
            dependencies = listOf(dep("A", "B"), dep("B", "C")),
        )
        assertEquals(listOf(id("A"), id("B"), id("C")), result)
    }

    @Test
    fun diamond() {
        val result = topologicalSort(
            taskIds = setOf(id("A"), id("B"), id("C"), id("D")),
            dependencies = listOf(dep("A", "B"), dep("A", "C"), dep("B", "D"), dep("C", "D")),
        )
        assertEquals(id("A"), result.first())
        assertEquals(id("D"), result.last())
        assertTrue(result.indexOf(id("B")) < result.indexOf(id("D")))
        assertTrue(result.indexOf(id("C")) < result.indexOf(id("D")))
    }

    @Test
    fun multipleRoots() {
        val result = topologicalSort(
            taskIds = setOf(id("A"), id("B"), id("C")),
            dependencies = listOf(dep("A", "C"), dep("B", "C")),
        )
        assertEquals(id("C"), result.last())
        assertTrue(result.indexOf(id("A")) < result.indexOf(id("C")))
        assertTrue(result.indexOf(id("B")) < result.indexOf(id("C")))
    }

    @Test
    fun singleNode() {
        val result = topologicalSort(
            taskIds = setOf(id("A")),
            dependencies = emptyList(),
        )
        assertEquals(listOf(id("A")), result)
    }

    @Test
    fun emptyGraph() {
        val result = topologicalSort(
            taskIds = emptySet(),
            dependencies = emptyList(),
        )
        assertEquals(emptyList(), result)
    }

    @Test
    fun cycleTwoNodes() {
        assertFailsWith<IllegalStateException> {
            topologicalSort(
                taskIds = setOf(id("A"), id("B")),
                dependencies = listOf(dep("A", "B"), dep("B", "A")),
            )
        }
    }

    @Test
    fun cycleThreeNodes() {
        assertFailsWith<IllegalStateException> {
            topologicalSort(
                taskIds = setOf(id("A"), id("B"), id("C")),
                dependencies = listOf(dep("A", "B"), dep("B", "C"), dep("C", "A")),
            )
        }
    }

    @Test
    fun partialDependencies_ignoredOutsideTaskIds() {
        val result = topologicalSort(
            taskIds = setOf(id("A")),
            dependencies = listOf(dep("A", "B")),
        )
        assertEquals(listOf(id("A")), result)
    }
}
