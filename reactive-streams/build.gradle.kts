import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    id("androidConfig")
}
// project.ext.artifactId = bt_name

android {
    defaultConfig {
        minSdk = Versions.MinSdkRX
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    namespace = "com.dbflow5.reactivestreams"
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi"
    )
}

dependencies {
    api(project(":lib"))
    api(libs.rx)
}

apply(from = "../kotlin-artifacts.gradle.kts")
