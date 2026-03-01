package com.khan366kos

import com.khan366kos.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(
    config: AppConfig = AppConfig(environment)
) {
    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureRouting(config)
}
