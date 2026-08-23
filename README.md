# DBFlow

[![JitPack.io](https://img.shields.io/badge/JitPack.io-5.0.0-alpha2-red.svg?style=flat)](https://jitpack.io/#agrosner/DBFlow)

A Kotlin Multiplatform SQLite library. Annotate tables and databases; a compiler plugin generates adapters and a typed query DSL.

**Targets:** Android, JVM, iOS, macOS.

```kotlin
@Database(version = 1, tables = [User::class])
abstract class AppDatabase : DBFlowDatabase<AppDatabase>() {
    abstract val userAdapter: ModelAdapter<User>
}

@Table
data class User(
    @PrimaryKey(autoincrement = true) val id: Int = 0,
    val name: String,
)
```

```kotlin
import com.dbflow5.database.createDB

val db = createDB<AppDatabase>(context) { copy(name = "App") }

db.writableTransaction {
    userAdapter.save(User(name = "Ada"))
    val users = (userAdapter.select() where (User_Table.name eq "Ada")).list()
}
```

## What’s included

| Module | Role |
| --- | --- |
| `lib` | Runtime, query DSL, transactions, `Flow` observers |
| `core` | Annotations and converters (pulled in by `lib`) |
| `compiler` + plugin `com.dbflow5` | Generates `*_Table`, `*_Database`, and `GeneratedDatabaseHolderFactory` |
| `sqlcipher` | Encrypted Android databases |
| `livedata` / `paging` / `reactive-streams` | Android extras |

Coroutines are built into `lib`. There is no separate coroutines artifact.

## Docs

Usage lives under [`usage2/`](usage2/README.md). The table of contents is in [`SUMMARY.md`](SUMMARY.md).

Published HTML: [dbflow.gitbook.io/dbflow](https://dbflow.gitbook.io/dbflow/)

## Install

See [Including in a project](usage2/including-in-project.md) for version catalogs, the compiler plugin, and generated sources.

Quick start with a version catalog:

```toml
[versions]
dbflow = "5.0.0-alpha2"

[libraries]
dbflow-lib = { module = "com.dbflow5:lib", version.ref = "dbflow" }
dbflow-compiler = { module = "com.dbflow5:compiler", version.ref = "dbflow" }

[plugins]
dbflow = { id = "com.dbflow5", version.ref = "dbflow" }
```

```kotlin
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.dbflow)
}

kotlin {
    sourceSets.named("commonMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/dbflow/commonMain/kotlin"))
        dependencies {
            implementation(libs.dbflow.lib)
        }
    }
}

configurations.configureEach {
    if (name.startsWith("kotlinCompilerPluginClasspath")) {
        dependencies.add(libs.dbflow.compiler.get())
    }
}
```

Until the plugin is on Maven Central, use a [composite build](usage2/including-in-project.md#from-this-repository) of this repo.

## Contribute

1. Match existing Kotlin style.
2. Keep the change scoped to the issue.
3. Open PRs against `master`.

## Maintainers

Started at [Raizlabs](https://www.raizlabs.com) / [Rightpoint](https://www.rightpoint.com). Maintained by [agrosner](https://github.com/agrosner).
