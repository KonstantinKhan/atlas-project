package com.khan366kos.atlas.project.backend.common.models.projectPlan

@JvmInline
value class ProjectPlanId(val value: String) {
    fun asString() = value

    companion object {
        val NONE = ProjectPlanId("")
    }
}