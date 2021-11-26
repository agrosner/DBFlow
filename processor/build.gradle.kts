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
    api(Dependencies.JavaPoet)
    api(Dependencies.KPoet)

    compileOnly(Dependencies.JavaXAnnotation)
    testImplementation(Dependencies.JUnit)
}

apply(from = "../kotlin-artifacts.gradle.kts")
