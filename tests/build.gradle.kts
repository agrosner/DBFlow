import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp") version Versions.KSP
    id("com.android.application")
    kotlin("multiplatform")
}

kotlin {
    jvm()
    android()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(project(":lib"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":lib"))
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
                implementation(Dependencies.Turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.appcompat:appcompat:1.4.0")
                implementation(project(":sqlcipher"))
                implementation(project(":reactive-streams"))
                implementation(project(":paging"))
                implementation(project(":livedata"))

            }
        }
        val androidTest by getting {
            dependsOn(androidMain)
            dependencies {
                implementation(Dependencies.JavaXAnnotation)
                implementation("org.mockito.kotlin:mockito-kotlin:4.0.0") {
                    exclude(group = "org.jetbrains.kotlin")
                }
                implementation("org.mockito:mockito-core:4.3.1")
                implementation("org.mockito:mockito-android:4.3.1")

                implementation(Dependencies.JUnit)
                implementation("androidx.test:core:1.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                implementation("androidx.arch.core:core-testing:2.1.0")
                implementation("androidx.test.ext:junit:1.1.3")
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(Dependencies.JUnit)
            }
        }
    }

    sourceSets.all {
        //kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
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
}

dependencies {
    val ksp = project(":ksp")
    val configs = listOf(
        "kspMetadata",
    )
    configs.forEach { config -> add(config, ksp) }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name != "kspKotlinMetadata") {
        dependsOn("kspKotlinMetadata")
    }
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=com.dbflow5.annotation.opts.InternalDBFlowApi",
        "-Xopt-in=com.dbflow5.annotation.opts.DelicateDBFlowApi"
    )
}
