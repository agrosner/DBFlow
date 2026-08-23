pluginManagement {
    includeBuild("compiler-gradle")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://www.jitpack.io")
    }
}

rootProject.name = "DBFlow"

include(
    ":lib",
    ":reactive-streams",
    ":livedata",
    ":processor",
    ":core",
    ":sqlcipher",
    ":tests",
    ":paging",
    ":ksp",
    ":ksp-tests",
    ":shared-model",
    ":kotlin-codegen",
    ":compiler",
)
