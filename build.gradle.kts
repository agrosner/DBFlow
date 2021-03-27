buildscript {
    repositories {
        jcenter()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin}")
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}