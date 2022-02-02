buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:3.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin}")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}