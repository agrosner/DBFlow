import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

configureJdk()

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf(
        "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
        "-Xopt-in=kotlin.ExperimentalStdlibApi"
    )
}

dependencies {
    api(project(":core"))
    api(project(":shared-model"))
    api(libs.kotlinpoet)
    api(libs.koin)
}

apply(from = "../kotlin-artifacts.gradle.kts")
