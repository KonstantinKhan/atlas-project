package com.khan366kos.atlas.project.backend.common.models.resource

@JvmInline
value class ResourceName(val value: String) {
    init {
        require(value.isNotBlank()) { "Resource name must not be blank" }
        require(value.length <= 255) { "Resource name must not exceed 255 characters" }
    }

    fun asString(): String = value

    companion object {
        val NONE: ResourceName = ResourceName("Unnamed")
    }
}
