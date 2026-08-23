# Including in a project

DBFlow is Kotlin Multiplatform. Apply the Gradle plugin; it wires everything else.

Requires **Kotlin 2.4+** and **Gradle 9+**. Version: **5.0.0-alpha2**.

## Version catalog

```toml
[versions]
dbflow = "5.0.0-alpha2"

[libraries]
# Optional extras only; the plugin adds com.dbflow5:lib itself.
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
}
```

That is the whole setup. Applying `com.dbflow5` configures:

- the `com.dbflow5:lib` dependency on `commonMain` (or `main` for single-target
  JVM/Android projects)
- the DBFlow compiler plugin on every Kotlin compilation
- `build/generated/dbflow/commonMain/kotlin` as a `commonMain` source directory
- `-lsqlite3` linker opts for every Kotlin/Native binary
- a `dbflowGenerate` task that runs before all Kotlin compilations

Single-target JVM (`kotlin("jvm")`), Android-only (`kotlin("android")`), and
native-only multiplatform projects work the same way — the plugin picks the
right source set and the native linker opts apply wherever native targets
exist.

### Generated sources

Generated Kotlin cannot be produced and compiled by the same compilation, so
the plugin registers a `dbflowGenerate` task: it compiles the model sources
with the DBFlow compiler plugin, keeps the generated Kotlin, and discards the
class output. Every Kotlin compilation runs after it, so main source sets —
and main artifacts — contain the generated adapters, `*_Database` classes, and
companion column properties.

Practical notes:

1. Put `@Table` / `@Database` in `commonMain` (or `main`).
2. Main code uses `select from User`, `User.name`, and `createDB<AppDatabase>()`
   — none of these name a generated type, so they compile in the generation
   pass too. Reference generated types (`*_Database`, `*_Adapter` helpers)
   directly from test source sets or downstream modules.
3. `dbflowGenerate` re-runs whenever models change or the generated directory
   is deleted; its output is tracked by content, so unchanged models keep
   downstream compilations up to date.
4. The generator resolves the JVM variant of your `commonMain` dependencies,
   so native-only projects need `commonMain` dependencies that also publish a
   JVM variant (most multiplatform libraries do).

Do not use KSP or KAPT for DBFlow. Those processors are leftover and not the consumer path.

## Artifacts

| Coordinate | Use |
| --- | --- |
| plugin `com.dbflow5` | The only thing you apply; wires everything below |
| `com.dbflow5:lib` | Runtime (includes `core` and coroutines); added by the plugin |
| `com.dbflow5:compiler` | Compiler plugin JAR; added by the plugin |
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
```

When DBFlow is in the same build (like this repo's `tests` module), substitute
the coordinates the plugin adds with the local projects instead:

```kotlin
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.dbflow5:lib")).using(project(":lib"))
        substitute(module("com.dbflow5:compiler")).using(project(":compiler"))
    }
}
```

## Next

[Getting started](gettingstarted.md)
