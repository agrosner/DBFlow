plugins {
    id("org.jetbrains.kotlin.jvm")
}

configureJdk(
    "com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
    "kotlin.ExperimentalStdlibApi",
    "com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview",
)

dependencies {
    api(project(":core"))
    api(libs.kotlinpoet)
    api(libs.javapoet)
    api(libs.kotlinpoet.javapoetInterop)
    api(libs.koin)
}
