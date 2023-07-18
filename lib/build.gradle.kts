plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("androidConfig")
    alias(libs.plugins.kotlinx.atomicfu)
}

// project.ext.artifactId = bt_name
kotlin {
    androidTarget()
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
                api(libs.coroutines)
                api(libs.atomicFu)
            }
        }
        val javaPlatformMain by creating

        val androidMain by getting {
            dependsOn(javaPlatformMain)
            dependencies {
                api(libs.coroutines.android)
            }
        }
        val jvmMain by getting {
            dependsOn(javaPlatformMain)
            dependencies {
                implementation(libs.sqliteJdbc)
                implementation(libs.hikariCp)
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.sqliter)
                implementation(libs.okio)
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
    namespace = "com.dbflow5.lib"
}

apply(from = "../kotlin-artifacts.gradle.kts")
