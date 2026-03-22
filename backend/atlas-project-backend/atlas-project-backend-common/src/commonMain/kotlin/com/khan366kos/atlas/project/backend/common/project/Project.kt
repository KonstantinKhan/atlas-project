package com.khan366kos.atlas.project.backend.common.project

data class Project(
    val id: ProjectId = ProjectId.NONE,
    val name: ProjectName = ProjectName.NONE
)
