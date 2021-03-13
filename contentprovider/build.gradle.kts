plugins {
    id("com.android.library")
    kotlin("android")
}

// project.ext.artifactId = bt_name

android {
    compileSdkVersion(Versions.TargetSdk)

    defaultConfig {
        minSdkVersion(Versions.MinSdk)
        targetSdkVersion(Versions.TargetSdk)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    api(project(":lib"))
    api(project(":contentprovider-annotations"))
    api(Dependencies.AndroidX.Annotations)
}

apply(from = "../kotlin-artifacts.gradle")

