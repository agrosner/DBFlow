plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))
    api(Dependencies.JavaPoet)
    api(Dependencies.KPoet)
    api(project(":shared-model"))
    api(Dependencies.Koin)

    compileOnly(Dependencies.JavaXAnnotation)
    testImplementation(Dependencies.JUnit)
}

apply(from = "../kotlin-artifacts.gradle.kts")
