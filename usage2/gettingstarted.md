# Getting started

## 1. Database and tables

List every table, view, query model, and migration on `@Database`. The generated class is `{Name}_Database`.

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

The plugin emits `AppDatabase_Database` (a `DBCreator`) and `User` (column properties).

## 2. Open the database

**Android**

```kotlin
class App : Application() {
    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = createDB<AppDatabase>(this) {
            copy(name = "App")
        }
    }
}
```

**JVM / Native**

```kotlin
val db = createDB<AppDatabase> {
    copy(name = "App")
}
```

The compiler plugin rewrites `createDB` to the generated factory. The Gradle plugin's `dbflowGenerate` task runs before every compilation, so this works from `commonMain` (see [install](including-in-project.md#generated-sources)).

`create` takes a `DBSettings` copy block: name, in-memory, journal mode, open helper, dispatchers.

Close with `db.close()` or `use { }`. `destroy()` closes and deletes the file.

## 3. Write and read

All adapter operations are `suspend` and run on the database dispatcher.

```kotlin
db.writableTransaction {
    userAdapter.save(User(name = "Ada"))

    val users = userAdapter.select().list()
    val ada = (userAdapter.select() where (User.name eq "Ada")).single()
}
```

`writableTransaction` is a write scope (`save`, `insert`, `update`, `delete`, plus reads). Use `readableTransaction` for reads only.

## 4. Inject the database

Pass `AppDatabase` (or its adapters) into repositories. Do not look up a global `FlowManager` — that API is gone.

```kotlin
class UserRepository(private val db: AppDatabase) {
    suspend fun usersNamed(name: String): List<User> = db.writableTransaction {
        (userAdapter.select() where (User.name eq name)).list()
    }
}
```

## Notes

- Models can be `data class`es with `val` properties.
- `@Table(allFields = true)` (default) maps every property. Mark the primary key. Use `@ColumnIgnore` to skip a property.
- Main code sticks to `createDB`, `select from User`, and column properties — none of these name a generated type. Reference `*_Database` / `*_Adapter` types directly from tests or downstream modules. See [install](including-in-project.md#generated-sources).
