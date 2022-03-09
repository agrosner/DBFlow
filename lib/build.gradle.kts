plugins {
    kotlin("multiplatform")
    id("com.android.library")
}
apply(plugin = "kotlinx-atomicfu")

// project.ext.artifactId = bt_name
kotlin {
    android()
    jvm()

    sourceSets {
        all {
            languageSettings.optIn("com.dbflow5.annotation.opts.DelicateDBFlowApi")
            languageSettings.optIn("com.dbflow5.annotation.opts.InternalDBFlowApi")
        }
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(Dependencies.Coroutines)
                api(Dependencies.AtomicFU)
            }
        }
        val javaPlatformMain by creating

        val androidMain by getting {
            dependsOn(javaPlatformMain)
            dependencies {
                api(Dependencies.CoroutinesAndroid)
            }
        }
        val jvmMain by getting {
            dependsOn(javaPlatformMain)
            dependencies {
                implementation(Dependencies.SQLiteJDBC)
                implementation(Dependencies.HikariCP)
                implementation(Dependencies.SLF4JApi)
                implementation(Dependencies.SLF4JSimple)
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
