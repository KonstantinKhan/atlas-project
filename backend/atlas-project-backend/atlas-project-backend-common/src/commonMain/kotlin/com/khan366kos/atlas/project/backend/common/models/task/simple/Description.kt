package com.khan366kos.atlas.project.backend.common.models.task.simple

@JvmInline
value class Description(val value: String) {
    init {
        require(value.length <= 10000) { "Description must not exceed 10000 characters" }
    }

    fun asString(): String = value

    companion object {
        val NONE: Description = Description("")
    }
}