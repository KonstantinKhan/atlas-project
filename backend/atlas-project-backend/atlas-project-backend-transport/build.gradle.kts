plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        jvmMain.dependencies {
            implementation(libs.kxs.ts.gen.core)
        }
    }
}

tasks.register<JavaExec>("generateTypeScript") {
    description = "Generate TypeScript interfaces from Kotlin @Serializable classes"
    group = "codegen"

    val jvmCompilation = kotlin.jvm().compilations["main"]
    classpath = files(
        jvmCompilation.output.allOutputs,
        jvmCompilation.runtimeDependencyFiles
    )
    mainClass.set("com.khan366kos.atlas.project.backend.transport.GenerateTypeScriptKt")

    val outputDir = file("${project.rootDir.parentFile.parentFile}/frontend/atlas-project-web-app/src/types/generated")
    args(outputDir.absolutePath)

    dependsOn("compileKotlinJvm")
}

