package com.khan366kos.atlas.project.backend.common.models.simple

@JvmInline
value class Duration(val value: String) {
    constructor(value: Int) : this(value.toString())
    fun asInt(): Int = value.toInt()
    companion object {
        val NONE = Duration("")
        val ZERO = Duration(0)
    }
}