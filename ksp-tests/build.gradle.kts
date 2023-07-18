plugins {
    id("com.google.devtools.ksp") version Versions.KSP
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("androidConfig")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin")
        getByName("test").kotlin.srcDir("build/generated/ksp/test/kotlin")
        getByName("androidTest").kotlin {
            srcDir("build/generated/ksp/debugAndroidTest/kotlin")
        }
    }
    namespace = "com.dbflow5.ksp.test"
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":sqlcipher"))
    implementation(project(":reactive-streams"))
    implementation(project(":paging"))
    implementation(project(":livedata"))

    testImplementation(libs.koin)

    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.koin.test)
    testImplementation(project(":ksp"))
    testImplementation(project(":processor"))
    kspTest(project(":ksp"))
    kaptTest(project(":processor"))
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(libs.kotlinCompileTesting.ksp)
    testImplementation(kotlin("test"))
    testImplementation(libs.javax.annotation)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.runner)
    testImplementation(libs.androidx.rules)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.junit)
}