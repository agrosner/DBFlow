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
}

apply(from = "../kotlin-artifacts.gradle.kts")
