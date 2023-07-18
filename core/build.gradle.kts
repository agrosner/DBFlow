import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("androidConfig")
}

kotlin {
    jvm()
    android()
    ios()
    macosArm64()
    macosX64()

    sourceSets {
        val commonMain by getting
        val jvmMain by getting
        val androidMain by getting {
            dependsOn(jvmMain)
        }
    }
}

// project.ext.artifactId = bt_name

android {
    compileSdk = Versions.TargetSdk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = Versions.MinSdk
        targetSdk = Versions.TargetSdk
    }
    namespace = "com.dbflow5.core"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

apply(from = "../kotlin-artifacts.gradle.kts")
