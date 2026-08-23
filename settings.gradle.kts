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
    ":core",
    ":sqlcipher",
    ":tests",
    ":paging",
    ":shared-model",
    ":kotlin-codegen",
    ":compiler",
)
