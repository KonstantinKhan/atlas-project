package com.khan366kos.atlas.project.backend.transport.enums

import kotlinx.serialization.Serializable

@Serializable
enum class DependencyTypeDto {
    FS, // Finish → Start
    SS, // Start → Start
    FF, // Finish → Finish
    SF, // Start → Finish
}