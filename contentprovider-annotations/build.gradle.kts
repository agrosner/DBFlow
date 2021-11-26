import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

// project.ext.artifactId = bt_name

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    api(project(":core"))
}

apply(from = "../kotlin-artifacts.gradle.kts")
