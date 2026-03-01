package com.khan366kos.atlas.project.backend.common.models.task.simple

@JvmInline
value class Title(val value: String) {
    init {
        require(value.isNotBlank()) { "Title must not be blank" }
        require(value.length <= 255) { "Title must not exceed 255 characters" }
    }

    fun asString(): String = value

    companion object {
        val NONE: Title = Title("Untitled")
    }
}