package com.khan366kos.atlas.project.backend.common.models.projectPlan

import com.khan366kos.atlas.project.backend.common.models.TaskDependency
import com.khan366kos.atlas.project.backend.common.models.task.simple.TaskId

fun topologicalSort(
    taskIds: Set<TaskId>,
    dependencies: Collection<TaskDependency>,
): List<TaskId> {
    val relevantDeps = dependencies.filter { it.predecessor in taskIds && it.successor in taskIds }

    val inDegree = taskIds.associateWith { 0 }.toMutableMap()
    val successors = taskIds.associateWith { mutableListOf<TaskId>() }.toMutableMap()

    for (dep in relevantDeps) {
        inDegree[dep.successor] = (inDegree[dep.successor] ?: 0) + 1
        successors.getOrPut(dep.predecessor) { mutableListOf() }.add(dep.successor)
    }

    val queue = ArrayDeque<TaskId>()
    for ((id, degree) in inDegree) {
        if (degree == 0) queue.add(id)
    }

    val result = mutableListOf<TaskId>()
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        result.add(current)
        for (succ in successors[current] ?: emptyList()) {
            val newDegree = (inDegree[succ] ?: 1) - 1
            inDegree[succ] = newDegree
            if (newDegree == 0) queue.add(succ)
        }
    }

    if (result.size != taskIds.size) {
        error("Cycle detected in task dependency graph")
    }

    return result
}
