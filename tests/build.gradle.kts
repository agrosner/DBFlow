import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("com.dbflow5")
}

kotlin {
    jvm()
    macosArm64()
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.dbflow5.test"
        compileSdk = Versions.TargetSdk
        minSdk = Versions.MinSdkRX
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withHostTest {}
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        packaging {
            resources {
                excludes.add("META-INF/services/javax.annotation.processing.Processor")
                excludes.add("META-INF/rxjava.properties")
                excludes.add("META-INF/DEPENDENCIES")
                excludes.add("META-INF/LICENSE")
                excludes.add("META-INF/LICENSE.txt")
                excludes.add("META-INF/license.txt")
                excludes.add("META-INF/NOTICE")
                excludes.add("META-INF/NOTICE.txt")
                excludes.add("META-INF/notice.txt")
                excludes.add("META-INF/AL2.0")
                excludes.add("META-INF/LGPL2.1")
                excludes.add("META-INF/*.kotlin_module")
                excludes.add("META-INF/licenses/**")
                excludes.add("**/**.dll")
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.dbflow5.annotation.opts.InternalDBFlowApi")
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
        invokeWhenCreated("nativeTest") {
            dependencies {
                implementation(libs.sqliter)
                implementation(libs.okio)
            }
        }
        getByName("androidMain").dependencies {
            implementation(libs.androidx.appcompat)
            implementation(project(":sqlcipher"))
            implementation(project(":reactive-streams"))
            implementation(project(":paging"))
            implementation(project(":livedata"))
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.runner)
            implementation(libs.androidx.rules)
            implementation(libs.androidx.core.testing)
            implementation(libs.androidx.junit)
            implementation(libs.javax.annotation)
            implementation(libs.mockito.kotlin)
            implementation(libs.mockito.core)
            implementation(libs.mockito.android)
            implementation(libs.robolectric)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.junit)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.runner)
            implementation(libs.androidx.rules)
            implementation(libs.androidx.core.testing)
            implementation(libs.androidx.junit)
            implementation(libs.javax.annotation)
            implementation(libs.mockito.kotlin)
            implementation(libs.mockito.core)
            implementation(libs.mockito.android)
        }
        getByName("jvmTest").dependencies {
            implementation(libs.junit)
            implementation(libs.mockito.kotlin)
            implementation(libs.mockito.core)
        }
    }
}

// The DBFlow plugin adds `com.dbflow5:lib` and `com.dbflow5:compiler` by
// coordinates; inside this build they resolve to the local projects.
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.dbflow5:lib")).using(project(":lib"))
        substitute(module("com.dbflow5:compiler")).using(project(":compiler"))
    }
}


tasks.withType<KotlinNativeTest>().configureEach {
    environment(
        "DBFLOW_RESOURCES_DIR",
        layout.projectDirectory.dir("src/commonMain/resources").asFile.absolutePath,
    )
}
