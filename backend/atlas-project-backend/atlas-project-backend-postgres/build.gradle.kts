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
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
