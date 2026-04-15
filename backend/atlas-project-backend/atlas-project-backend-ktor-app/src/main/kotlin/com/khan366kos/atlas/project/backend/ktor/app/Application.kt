package com.khan366kos.atlas.project.backend.ktor.app

import com.khan366kos.atlas.project.backend.ktor.app.plugins.configureRouting
import com.khan366kos.config.AppConfig
import com.khan366kos.configureHTTP
import com.khan366kos.configureRoutingOld
import com.khan366kos.configureSerialization
import com.khan366kos.configureStatusPages
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
    configureRoutingOld(config)
    configureRouting(config)
}
