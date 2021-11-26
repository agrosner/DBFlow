plugins {
    id("com.android.library")
    kotlin("android")
}

// project.ext.artifactId = bt_name

android {
    compileSdkVersion(Versions.TargetSdk)
    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(Versions.TargetSdk)
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":lib"))
    api(Dependencies.Coroutines)
}

apply(from = "../kotlin-artifacts.gradle.kts")
