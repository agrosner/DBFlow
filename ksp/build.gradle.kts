import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf(
        "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
        "-Xopt-in=kotlin.ExperimentalStdlibApi"
    )
}

dependencies {
    api(project(":core"))
    api(Dependencies.KotlinPoet)
    api(Dependencies.Koin)
    api(Dependencies.KSP)

    testImplementation(Dependencies.MockitoKotlin)
    testImplementation(Dependencies.KoinTest)
    testImplementation(kotlin("test"))
    testImplementation(Dependencies.KotlinCompileTestingKSP)
}

apply(from = "../kotlin-artifacts.gradle.kts")
