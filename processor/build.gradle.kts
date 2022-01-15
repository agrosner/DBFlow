plugins {
    kotlin("jvm")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf(
        "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
        "-Xopt-in=kotlin.ExperimentalStdlibApi",
        "-Xopt-in=com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview",
    )
}

dependencies {
    api(project(":core"))
    api(Dependencies.JavaPoet)
    api(Dependencies.KPoet)
    api(project(":shared-model"))
    api(Dependencies.Koin)

    compileOnly(Dependencies.JavaXAnnotation)
    testImplementation(Dependencies.JUnit)
    testImplementation(Dependencies.KoinTest)
    testImplementation(Dependencies.MockitoKotlin)
    testImplementation(kotlin("test"))
}

apply(from = "../kotlin-artifacts.gradle.kts")
