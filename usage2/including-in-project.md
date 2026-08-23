# Including in a project

DBFlow is Kotlin Multiplatform. Apply the Gradle plugin, put `lib` on `commonMain`, and compile generated sources.

Requires **Kotlin 2.4+** and **Gradle 9+**. Version: **5.0.0-alpha2**.

## Version catalog

```toml
[versions]
dbflow = "5.0.0-alpha2"

[libraries]
dbflow-lib = { module = "com.dbflow5:lib", version.ref = "dbflow" }
dbflow-compiler = { module = "com.dbflow5:compiler", version.ref = "dbflow" }
dbflow-sqlcipher = { module = "com.dbflow5:sqlcipher", version.ref = "dbflow" }
dbflow-livedata = { module = "com.dbflow5:livedata", version.ref = "dbflow" }
dbflow-paging = { module = "com.dbflow5:paging", version.ref = "dbflow" }
dbflow-reactive-streams = { module = "com.dbflow5:reactive-streams", version.ref = "dbflow" }

[plugins]
dbflow = { id = "com.dbflow5", version.ref = "dbflow" }
```

## Settings

Use `pluginManagement` and `dependencyResolutionManagement`. Do not declare repositories in `allprojects`.

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

JitPack module coordinates use `com.github.agrosner.DBFlow` if `com.dbflow5` is not resolved from your repository.

## Module build

```kotlin
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.dbflow)
}

kotlin {
    android { /* … */ }
    jvm()
    iosSimulatorArm64()
    macosArm64()

    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/dbflow/commonMain/kotlin"))
            dependencies {
                implementation(libs.dbflow.lib)
            }
        }
    }
}

configurations.configureEach {
    if (name.startsWith("kotlinCompilerPluginClasspath")) {
        dependencies.add(libs.dbflow.compiler.get())
    }
}
```

Native targets need SQLite at link time:

```kotlin
targets.withType<KotlinNativeTarget>().configureEach {
    binaries.all { linkerOpts("-lsqlite3") }
}
```

### Generated sources

The plugin writes Kotlin to `build/generated/dbflow/commonMain/kotlin` at the **end** of a compilation. That compilation cannot see the new types.

Practical setup:

1. Put `@Table` / `@Database` in `commonMain`.
2. Consume `*_Table` / `*_Database` in a **later** compilation — `commonTest`, another module, or a compile that `dependsOn` metadata generation.

The test module in this repo generates during `compileKotlinMetadata`, then platform compiles depend on that task and add the generated directory to `commonTest`.

Do not use KSP or KAPT for DBFlow. Those processors are leftover and not the consumer path.

## Artifacts

| Coordinate | Use |
| --- | --- |
| `com.dbflow5:lib` | Runtime (includes `core` and coroutines) |
| `com.dbflow5:compiler` | Compiler plugin JAR |
| plugin `com.dbflow5` | Wires the plugin and `generatedDir` |
| `com.dbflow5:sqlcipher` | Encrypted Android helper |
| `com.dbflow5:livedata` | `toLiveData()` |
| `com.dbflow5:paging` | `toDataSourceFactory()` |
| `com.dbflow5:reactive-streams` | RxJava 3 |

`org.gradle.isolated-projects` is currently incompatible with the plugin.

## From this repository

Until the plugin is published, include the Gradle plugin and compiler as composites:

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("../DBFlow/compiler-gradle")
}

includeBuild("../DBFlow") {
    dependencySubstitution {
        substitute(module("com.dbflow5:lib")).using(project(":lib"))
        substitute(module("com.dbflow5:compiler")).using(project(":compiler"))
    }
}
```

```kotlin
plugins {
    id("com.dbflow5")
}

configurations.configureEach {
    if (name.startsWith("kotlinCompilerPluginClasspath")) {
        dependencies.add(project.dependencies.create("com.dbflow5:compiler:5.0.0-alpha2"))
    }
}
```

Or add `project(":compiler")` when DBFlow is in the same build.

## Next

[Getting started](gettingstarted.md)
