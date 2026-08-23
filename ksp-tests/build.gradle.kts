plugins {
    id("androidConfig")
    alias(libs.plugins.ksp)
    alias(libs.plugins.legacy.kapt)
}

android {
    namespace = "com.dbflow5.ksp.test"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

configureJdk()

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
