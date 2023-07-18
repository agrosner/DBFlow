import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

configureJdk()

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf(
        "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
        "-Xopt-in=kotlin.ExperimentalStdlibApi",
        "-Xopt-in=com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview",
    )
}

dependencies {
    api(project(":core"))
    api(libs.kotlinpoet)
    api(libs.javapoet)
    api(libs.kotlinpoet.javapoetInterop)
    api(libs.koin)
}

apply(from = "../kotlin-artifacts.gradle.kts")
