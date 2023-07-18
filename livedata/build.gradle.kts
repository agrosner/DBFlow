import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    id("androidConfig")
}
// project.ext.artifactId = bt_name

android {
    defaultConfig {
        minSdk = Versions.ArchMin
    }
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    namespace = "com.dbflow5.livedata"
}

dependencies {
    implementation(project(":lib"))
    api(libs.androidx.livedata)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi"
    )
}

apply(from = "../kotlin-artifacts.gradle.kts")
