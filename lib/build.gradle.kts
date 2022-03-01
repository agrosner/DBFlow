import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}
// project.ext.artifactId = bt_name
kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(Dependencies.Coroutines)
            }
        }
        val androidMain by getting {
            dependencies {
                api(Dependencies.CoroutinesAndroid)
            }
        }
    }
}

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
        freeCompilerArgs = listOf(
            "-Xopt-in=com.dbflow5.annotation.opts.DelicateDBFlowApi",
            "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi"
        )
    }
}

apply(from = "../kotlin-artifacts.gradle.kts")
