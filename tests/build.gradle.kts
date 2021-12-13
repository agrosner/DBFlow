import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp") version Versions.KSP
    id("com.android.application")
    kotlin("android")
    id("com.getkeepsafe.dexcount")
    kotlin("kapt")
}

android {

    useLibrary("org.apache.http.legacy")

    compileSdk = Versions.TargetSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        minSdk = Versions.MinSdkRX
        targetSdk = Versions.TargetSdk
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        exclude("META-INF/services/javax.annotation.processing.Processor")
        exclude("META-INF/rxjava.properties")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/AL2.0")
        exclude("META-INF/LGPL2.1")
        exclude("META-INF/*.kotlin_module")
    }

    sourceSets {
        getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin")
        getByName("test").kotlin.srcDir("build/generated/ksp/test/kotlin")
        getByName("androidTest").kotlin.srcDir("build/generated/ksp/androidTest/kotlin")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation(project(":lib"))
    implementation(project(":sqlcipher"))
    implementation(project(":reactive-streams"))
    implementation(project(":coroutines"))
    implementation(project(":paging"))
    implementation(project(":livedata"))

    //kaptAndroidTest(project(":processor"))
    kspAndroidTest(project(":ksp"))

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(Dependencies.JavaXAnnotation)
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    androidTestImplementation("org.mockito:mockito-android:2.23.0")

    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2")
    androidTestImplementation(Dependencies.JUnit)
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")

}

dexcount {
    includeClasses.set(true)
    orderByMethodCount.set(true)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
}