plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.datetime)

    implementation(projects.atlasProjectBackendCommon)
    implementation(projects.atlasProjectBackendTransport)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}