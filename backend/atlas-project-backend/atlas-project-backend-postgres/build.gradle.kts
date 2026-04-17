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
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
    val colimaSocket = "${System.getProperty("user.home")}/.colima/default/docker.sock"
    if (File(colimaSocket).exists()) {
        environment("DOCKER_HOST", "unix://$colimaSocket")
        environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
        systemProperty("DOCKER_HOST", "unix://$colimaSocket")
        jvmArgs("-DDOCKER_HOST=unix://$colimaSocket")
    }
}
