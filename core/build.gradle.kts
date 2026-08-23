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
        namespace = "com.dbflow5.core"
        compileSdk = Versions.TargetSdk
        minSdk = Versions.MinSdk
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val javaPlatformMain = create("javaPlatformMain") {
            dependsOn(getByName("commonMain"))
        }
        getByName("androidMain").dependsOn(javaPlatformMain)
        getByName("jvmMain").dependsOn(javaPlatformMain)
    }
}
