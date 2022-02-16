import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

    lintOptions {
        isAbortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xopt-in=com.dbflow5.annotation.opts.DelicateDBFlowApi",
            "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi"
        )
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

dependencies {
    api(project(":core"))
    api(Dependencies.AndroidX.Annotations)
    api(Dependencies.Coroutines)
    api(Dependencies.CoroutinesAndroid)
}

apply(from = "../kotlin-artifacts.gradle.kts")
