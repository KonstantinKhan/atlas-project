package com.khan366kos.atlas.project.backend.common.models.project

import com.khan366kos.atlas.project.backend.common.models.project.ProjectId
import com.khan366kos.atlas.project.backend.common.models.project.ProjectName

data class Project(
    val id: ProjectId = ProjectId.Companion.NONE,
    val name: ProjectName = ProjectName.Companion.NONE,
) {
    companion object {
        val NONE = Project()
    }
}