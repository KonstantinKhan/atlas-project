package com.khan366kos.atlas.project.backend.common.exceptions

class ProjectNotFoundException(id: String) : RuntimeException("Project not found: $id")

class ProjectOperationFailedException(cause: Throwable) : RuntimeException("Project operation failed", cause)