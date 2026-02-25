package com.khan366kos.atlas.project.backend.transport.enums

import kotlinx.serialization.Serializable

@Serializable
enum class DependencyTypeDto(val value: String) {
    FS("FS"), // Finish → Start
    SS("SS"), // Start → Start
    FF("FF"), // Finish → Finish
    SF("SF")  // Start → Finish
}