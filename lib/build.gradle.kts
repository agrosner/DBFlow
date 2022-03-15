plugins {
    kotlin("multiplatform")
    id("com.android.library")
}
apply(plugin = "kotlinx-atomicfu")

// project.ext.artifactId = bt_name
kotlin {
    android()
    jvm()
    ios()
    macosArm64()
    macosX64()

    sourceSets {
        all {
            languageSettings.optIn("com.dbflow5.annotation.opts.DelicateDBFlowApi")
            languageSettings.optIn("com.dbflow5.annotation.opts.InternalDBFlowApi")
            languageSettings.optIn("kotlinx.coroutines.DelicateCoroutinesApi")
        }
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(Dependencies.Coroutines)
                api(Dependencies.AtomicFU)
                implementation(Dependencies.StatelyISO)
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

        val nativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(Dependencies.SQLiter)
                implementation(Dependencies.OkIO)
            }
        }

        val iosMain by getting {
            dependsOn(nativeMain)
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosArm64Main by getting {
            dependsOn(nativeMain)
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
