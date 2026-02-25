package com.khan366kos.atlas.project.backend.common.models.task.simple

@JvmInline
value class Description(val value: String)  {
    fun asString(): String  = value

    companion object {
        val NONE = Description("")
    }
}