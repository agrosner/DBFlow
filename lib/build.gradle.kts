plugins {
    kotlin("multiplatform")
    id("com.android.library")
}
apply(plugin = "kotlinx-atomicfu")

// project.ext.artifactId = bt_name
kotlin {
    android()

    sourceSets {
        all {
            languageSettings.optIn("com.dbflow5.annotation.opts.DelicateDBFlowApi")
            languageSettings.optIn("com.dbflow5.annotation.opts.InternalDBFlowApi")
        }
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

apply(from = "../kotlin-artifacts.gradle.kts")
