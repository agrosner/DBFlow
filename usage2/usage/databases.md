# Databases

## Declare

```kotlin
@Database(
    version = 1,
    foreignKeyConstraintsEnforced = true,
    tables = [User::class, Post::class],
    views = [AuthorView::class],
    queries = [UserNameQuery::class],
    migrations = [AddEmailMigration::class],
)
abstract class AppDatabase : DBFlowDatabase<AppDatabase>() {
    abstract val userAdapter: ModelAdapter<User>
    abstract val postAdapter: ModelAdapter<Post>
    abstract val authorViewAdapter: ViewAdapter<AuthorView>
    abstract val userNameQueryAdapter: QueryAdapter<UserNameQuery>
}
```

The generated type is `AppDatabase_Database`. Its companion implements `DBCreator<AppDatabase>`.

Default file name is the class name plus `.db` (`AppDatabase.db`). Override in settings.

A table belongs to one database. List it on `@Database` (preferred) or set `@Table(database = AppDatabase::class)`.

## Open

Call `DatabaseObjectLookup.loadHolder(GeneratedDatabaseHolderFactory)` once per process before KClass lookups (`select from User::class`). Creating via `AppDatabase_Database.create` uses the generated factory directly.

```kotlin
// Android
val db = AppDatabase_Database.create(context) {
    copy(name = "App", inMemory = false)
}

// JVM
val db = AppDatabase_Database.create {
    copy(name = "App")
}

// Explicit platform settings (all targets)
val db = AppDatabase_Database.create(DBPlatformSettings()) {
    copy(name = "App")
}
```

`DBSettings` fields you typically copy:

| Field | Default | Purpose |
| --- | --- | --- |
| `name` | database class name | File stem (must match `[A-Za-z_$][A-Za-z0-9_$]*`) |
| `inMemory` | `false` | Tests / short-lived DBs |
| `databaseExtensionName` | `".db"` | File suffix |
| `journalMode` | `Automatic` | WAL on capable devices |
| `openHelperCreator` | platform SQLite | SQLCipher or a fake in tests |
| `databaseCallback` | `null` | Open / upgrade hooks |
| `throwExceptionsOnCreate` | `true` | Fail fast on create errors |

The first access to `writableDatabase` opens the file and runs migrations.

```kotlin
db.use {
    writableDatabase // force open
}
```

`close()` stops the dispatcher and closes the connection. `destroy()` also deletes the file.

## Transactions

```kotlin
db.writableTransaction {
    userAdapter.save(user)
}

db.readableTransaction {
    userAdapter.select().list()
}
```

Both hop to `transactionCoroutineDispatcher` (single-thread executor by default). Nested work on the same database stays on that dispatcher.

Callbacks (async `Transaction` success / error) use `callbackDispatcher` (`Dispatchers.Main` on Android and JVM).

## Platforms

| Target | Open helper | Notes |
| --- | --- | --- |
| Android | `AndroidSQLiteOpenHelper` | `create(context)` needs `Context` |
| JVM | JDBC + sqlite-jdbc | `create { }` |
| Native | sqliter | Link `-lsqlite3` |

## Multiple databases

Declare a second `@Database` and create it the same way. Do not share `@Table` types across databases.
