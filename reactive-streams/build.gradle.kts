plugins {
    id("com.android.library")
    kotlin("android")
}
// project.ext.artifactId = bt_name

android {
    compileSdkVersion(Versions.TargetSdk)

    defaultConfig {
        minSdkVersion(Versions.MinSdkRX)
        targetSdkVersion(Versions.TargetSdk)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi"
    )
}

dependencies {
    api(project(":lib"))
    api(Dependencies.RX)
}

apply(from = "../kotlin-artifacts.gradle.kts")
