plugins {
    id("org.jetbrains.kotlin.jvm")
}

configureJdk(
    "com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
    "kotlin.ExperimentalStdlibApi",
    "com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview",
    "com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview",
)

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
