package com.khan366kos.atlas.project.backend.common.simple

@JvmInline
value class Id(val value: String) {
    fun asInt(): Int = value.toInt()

    companion object {
        val NONE = Id("")
    }
}