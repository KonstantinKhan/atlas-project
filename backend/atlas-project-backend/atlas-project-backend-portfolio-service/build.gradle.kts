plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.atlasProjectBackendCommon)
    implementation(projects.atlasProjectBackendTransport)
    implementation(projects.atlasProjectBackendMappers)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}