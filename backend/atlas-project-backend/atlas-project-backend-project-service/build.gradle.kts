plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.atlasProjectBackendCommon)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}