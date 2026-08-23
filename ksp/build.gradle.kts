plugins {
    id("org.jetbrains.kotlin.jvm")
}

configureJdk(
    "com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
    "kotlin.ExperimentalStdlibApi",
)

dependencies {
    api(project(":core"))
    api(project(":shared-model"))
    api(project(":kotlin-codegen"))
    api(libs.kotlinpoet)
    api(libs.koin)
    api(libs.ksp)
}
