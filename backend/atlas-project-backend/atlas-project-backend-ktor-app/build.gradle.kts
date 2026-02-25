plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.kotlinx.datetime)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(projects.atlasProjectBackendTransport)
    implementation(projects.atlasProjectBackendCommon)
    implementation(projects.atlasProjectBackendMappers)
    implementation(projects.atlasProjectBackendPostgres)
    implementation(projects.atlasProjectBackendCalendarService)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
