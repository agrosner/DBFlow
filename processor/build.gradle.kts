plugins {
    kotlin("jvm")
}

configureJdk()

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf(
        "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
        "-Xopt-in=kotlin.ExperimentalStdlibApi",
        "-Xopt-in=com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview",
        "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview",
    )
}

dependencies {
    api(project(":core"))
    api(libs.javapoet)
    api(project(":shared-model"))
    api(libs.koin)
    api(project(":kotlin-codegen"))
    api(libs.kotlinpoet.metadata)

    implementation(kotlin("reflect"))

    compileOnly(libs.javax.annotation)
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(kotlin("test"))
}

apply(from = "../kotlin-artifacts.gradle.kts")
