import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    android {
        namespace = "com.dbflow5.lib"
        compileSdk = Versions.TargetSdk
        minSdk = Versions.MinSdk
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("com.dbflow5.annotation.opts.DelicateDBFlowApi")
            languageSettings.optIn("com.dbflow5.annotation.opts.InternalDBFlowApi")
            languageSettings.optIn("kotlinx.coroutines.DelicateCoroutinesApi")
        }
        commonMain.dependencies {
            api(project(":core"))
            api(libs.coroutines)
            api(libs.atomicFu)
        }
        val javaPlatformMain = create("javaPlatformMain") {
            dependsOn(getByName("commonMain"))
        }
        getByName("androidMain") {
            dependsOn(javaPlatformMain)
            dependencies {
                api(libs.coroutines.android)
            }
        }
        getByName("jvmMain") {
            dependsOn(javaPlatformMain)
            dependencies {
                implementation(libs.sqliteJdbc)
                implementation(libs.hikariCp)
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)
            }
        }
        val nativeMain = create("nativeMain") {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.sqliter)
                implementation(libs.okio)
            }
        }
        val iosMain = create("iosMain") {
            dependsOn(nativeMain)
        }
        listOf("iosArm64Main", "iosSimulatorArm64Main").forEach { name ->
            getByName(name).dependsOn(iosMain)
        }
        getByName("macosArm64Main").dependsOn(nativeMain)
    }
}
