plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.khan366kos"
version = "0.0.1"

dependencies {
    implementation(projects.atlasProjectBackendCommon)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.postgresql)
    implementation(libs.h2)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
