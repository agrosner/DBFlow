import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

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
    api(Dependencies.KotlinPoet)
    api(Dependencies.JavaPoet)
    api(Dependencies.KotlinPoetJavaPoetInterop)
    api(Dependencies.Koin)
}

apply(from = "../kotlin-artifacts.gradle.kts")
