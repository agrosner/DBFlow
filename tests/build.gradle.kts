plugins {
    id("com.android.application")
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version Versions.KSP
}
configureJdk()

kotlin {
    jvm()
    androidTarget()
    macosArm64()
    ios()

    targets.getByName("macosArm64") {
        compilations.getByName("test") {
            kotlinOptions {
                freeCompilerArgs += listOf("-linker-options", "-lsqlite3")
            }
        }
    }

    targets.getByName("iosX64") {
        compilations.getByName("test") {
            kotlinOptions {
                freeCompilerArgs += listOf("-linker-options", "-lsqlite3")
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.dbflow5.annotation.opts.InternalDBFlowApi")
        }
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
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(project(":sqlcipher"))
                implementation(project(":reactive-streams"))
                implementation(project(":paging"))
                implementation(project(":livedata"))

            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.coroutines.android)
                implementation(libs.javax.annotation)
                implementation(libs.mockito.kotlin)
                implementation(libs.mockito.core)
                implementation(libs.mockito.android)

                implementation(libs.junit)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.runner)
                implementation(libs.androidx.rules)
                implementation(libs.androidx.core.testing)
                implementation(libs.androidx.junit)
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockito.kotlin)
                implementation(libs.mockito.core)
            }
        }

        val macosArm64Main by getting {
            dependsOn(commonMain)
        }
        val macosArm64Test by getting {
            dependsOn(commonTest)
        }
        val iosTest by getting
    }
}

android {
    configureVersions()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    useLibrary("org.apache.http.legacy")

    defaultConfig {
        minSdk = Versions.MinSdkRX
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
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
    namespace = "com.dbflow5.test"
}

dependencies {
    val processor = project(":ksp")
    listOf(
        "kspCommonMainMetadata",
    ).forEach { config ->
        add(config, processor)
    }
}
