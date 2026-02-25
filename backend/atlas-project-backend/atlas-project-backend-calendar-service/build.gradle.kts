plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines)

    implementation(libs.kotlinx.datetime)

    implementation(projects.atlasProjectBackendCommon)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}