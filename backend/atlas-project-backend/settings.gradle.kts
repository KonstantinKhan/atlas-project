rootProject.name = "atlas-project-backend"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("atlas-project-backend-ktor-app")
include("atlas-project-backend-common")
include("atlas-project-backend-transport")
include("atlas-project-backend-repo-in-memory")