package com.khan366kos

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    val repo = configureDatabases()
    configureHTTP()
    configureRouting(repo)
}
