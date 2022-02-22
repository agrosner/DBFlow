import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp") version Versions.KSP
    id("com.android.application")
    kotlin("android")
    id("com.getkeepsafe.dexcount")
    kotlin("kapt")
}

kapt {
    // needed to use generated types properly.
    correctErrorTypes = true
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
        resources.excludes.addAll(
            listOf(
                "META-INF/services/javax.annotation.processing.Processor",
                "META-INF/rxjava.properties",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "META-INF/licenses/**",
                "**/**.dll"
            )
        )
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
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation(project(":lib"))
    implementation(project(":sqlcipher"))
    implementation(project(":reactive-streams"))
    implementation(project(":paging"))
    implementation(project(":livedata"))

    kaptAndroidTest(project(":processor"))
    //kspAndroidTest(project(":ksp"))

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(Dependencies.JavaXAnnotation)
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    androidTestImplementation("org.mockito:mockito-core:4.3.1")
    androidTestImplementation("org.mockito:mockito-android:4.3.1")

    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2")
    androidTestImplementation(Dependencies.JUnit)
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation(Dependencies.Turbine)
}

dexcount {
    includeClasses.set(true)
    orderByMethodCount.set(true)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi",
        "-Xopt-in=com.dbflow5.annotation.opts.DelicateDBFlowApi"
    )
}