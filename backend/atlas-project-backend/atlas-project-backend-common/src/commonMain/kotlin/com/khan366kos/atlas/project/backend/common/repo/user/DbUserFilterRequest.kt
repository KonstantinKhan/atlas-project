package com.khan366kos.atlas.project.backend.common.repo.user

import com.khan366kos.atlas.project.backend.common.models.user.UserRole

class DbUserFilterRequest(
    val name: String? = null,
    val role: UserRole? = null,
)
