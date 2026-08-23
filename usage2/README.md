# Usage

DBFlow maps Kotlin types to SQLite. You declare a `@Database` and `@Table`s. The compiler plugin generates:

- `{Name}_Database` — concrete database + `DBCreator`
- Model companions — `User.name` column properties, `select from User`, and the
  runtime `ModelAdapter` (`User.Companion as ModelAdapter<User>`)
- `{Name}_Adapter` helpers — internal `TableOps`, binders, and property getters wired into companions

Work through generated **adapters** on the database instance. Reads and writes are **suspend** and run on the database’s transaction dispatcher.

```kotlin
db.writableTransaction {
    userAdapter.save(User(name = "Ada"))
    val ada = (userAdapter.select() where (User.name eq "Ada")).single()
}
```

## Concepts

| Topic | Notes |
| --- | --- |
| [Install](including-in-project.md) | Gradle plugin, version catalog, generated sources |
| [Databases](usage/databases.md) | Open, settings, platforms |
| [Models](usage/models.md) | Tables, keys, columns |
| [Writes](usage/storingdata.md) | `save` / `insert` / `update` / `delete` |
| [Queries](usage/retrieval.md) | `select()`, `list()`, `single()` |
| [Relationships](usage/relationships.md) | Foreign keys, one-to-many, many-to-many |
| [Migrations](usage/migrations.md) | Versioned schema changes |
| [Observability](usage/observability.md) | `Flow` on table changes |

Android-only extras: [LiveData, Paging, RxJava](android.md), [SQLCipher](advanced-usage/sqlciphersupport.md).
