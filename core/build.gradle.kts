import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    jvm()
    android()

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
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

apply(from = "../kotlin-artifacts.gradle.kts")
