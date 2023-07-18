import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    id("androidConfig")
}
// project.ext.artifactId = bt_name

android {
    defaultConfig {
        minSdk = Versions.SQLCipherMin
    }
    namespace = "com.dbflow5.sqlcipher"
}

dependencies {
    api(libs.sqlCipher)
    api(libs.androidx.sqlite)
    api(project(":lib"))
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi"
    )
}

apply(from = "../kotlin-artifacts.gradle.kts")
