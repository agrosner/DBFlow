plugins {
    id("com.google.devtools.ksp") version Versions.KSP
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {

    compileSdk = Versions.TargetSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        minSdk = Versions.MinSdkRX
        targetSdk = Versions.TargetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin")
        getByName("test").kotlin.srcDir("build/generated/ksp/test/kotlin")
        getByName("androidTest").kotlin {
            srcDir("build/generated/ksp/debugAndroidTest/kotlin")
        }
    }
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":sqlcipher"))
    implementation(project(":reactive-streams"))
    implementation(project(":paging"))
    implementation(project(":livedata"))

    testImplementation(Dependencies.Koin)

    testImplementation(Dependencies.MockitoKotlin)
    testImplementation(Dependencies.KoinTest)
    testImplementation(project(":ksp"))
    testImplementation(project(":processor"))
    kspTest(project(":ksp"))
    kaptTest(project(":processor"))
    testImplementation(Dependencies.KotlinCompileTesting)
    testImplementation(Dependencies.KotlinCompileTestingKSP)
    testImplementation(kotlin("test"))
    testImplementation(Dependencies.JavaXAnnotation)
    testImplementation(Dependencies.JUnit)
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test:rules:1.4.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.test.ext:junit:1.1.3")
}