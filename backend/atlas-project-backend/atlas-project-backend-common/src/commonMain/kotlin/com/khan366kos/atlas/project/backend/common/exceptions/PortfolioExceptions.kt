package com.khan366kos.atlas.project.backend.common.exceptions

class PortfolioNotFoundException(id: String) : RuntimeException("Portfolio not found: $id")

class PortfolioOperationFailedException(cause: Throwable) : RuntimeException("Portfolio operation failed", cause)