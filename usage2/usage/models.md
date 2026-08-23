# Models

A table is a Kotlin class with `@Table` and at least one `@PrimaryKey`.

```kotlin
@Table
data class User(
    @PrimaryKey(autoincrement = true) val id: Int = 0,
    val name: String,
    val email: String?,
)
```

Register it on `@Database(tables = [User::class])`.

Generated:

- **`User.Companion`** — column properties (`User.name`), `select from User`, and the
  `ModelAdapter<User>` used at runtime (`User.Companion as ModelAdapter<User>`)
- **`User_Adapter.kt`** — internal helpers (`TableOps`, binders, property getters) wired into the companion
- **`userAdapter`** — abstract `val` on your database class; the generated `create()` passes the companion

You do not reference `*_Table` types or adapter delegate objects in app code.

## Columns

`@Table(allFields = true)` (default) maps every property. Use `@ColumnIgnore` to skip one. Set `allFields = false` and annotate each stored property with `@Column`, `@PrimaryKey`, or `@ForeignKey`.

```kotlin
@Table
data class User(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnIgnore val transientFlag: Boolean = false,
)
```

Rename a column with `@Column(name = "first_name")`.

Nullability is respected. Non-null properties are not assigned `null` from a cursor.

### Supported types

- Primitives and their nullable counterparts
- `String`, `Boolean`
- `com.dbflow5.data.Blob`
- Types with a [TypeConverter](typeconverters.md)
- Other tables only as `@ForeignKey` (or `@PrimaryKey` + `@ForeignKey`)

Not supported as columns: `List`, `Map`, or other generic containers. Use a related table.

## Primary keys

One or more `@PrimaryKey`. Autoincrement: `@PrimaryKey(autoincrement = true)` — only one, and do not mix with other primary keys.

```kotlin
@Table
data class Membership(
    @PrimaryKey val userId: String,
    @PrimaryKey val groupId: String,
)
```

## Unique

```kotlin
@Table
data class User(
    @PrimaryKey val id: Int = 0,
    @Unique val email: String,
)
```

Composite unique:

```kotlin
@Table(
    uniqueColumnGroups = [
        UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
    ]
)
data class Enrollment(
    @PrimaryKey val id: Int = 0,
    @Unique(unique = false, uniqueGroups = [1]) val studentId: Int,
    @Unique(unique = false, uniqueGroups = [1]) val courseId: Int,
)
```

## Defaults

`@Column(defaultValue = "…")` is the SQL default used when the property is `null` on save — not a Kotlin constructor default.

## FTS

`@Fts3` / `@Fts4` mark virtual tables. See the [SQLite FTS docs](https://www.sqlite.org/fts3.html).

## Temporary / deferred create

```kotlin
@Table(temporary = true, createWithDatabase = false)
data class Scratch(@PrimaryKey val id: Int)
```

`createWithDatabase = false` keeps the generated adapter but does not `CREATE TABLE` at open (legacy tables for migrations).

## Constructor

A no-arg constructor is required for codegen. `data class` properties need defaults when they are not nullable, or supply a secondary constructor. `val` properties are fine.

## Model companions

The compiler plugin adds a companion object to each `@Table`, `@ModelView`, and `@Query` class:

```kotlin
@Table
data class User(@PrimaryKey val id: Int, val name: String)

// Generated conceptually:
// companion object : ModelAdapterImpl<User>(), AdapterCompanion<User> {
//     val name: PropertyStart<String, User>  // column property
// }
```

Use `User.name` in `where`, `set`, and `insert` clauses. Use `userAdapter` (or
`User.Companion as ModelAdapter<User>`) for `save`, `select`, and other adapter
operations. Both refer to the same runtime object.

`@Query` and `@ModelView` companions extend `QueryAdapterImpl` and
`ViewAdapterImpl` respectively.

### `DBRepresentable.name` vs column `name`

`DBRepresentable.name` is the quoted SQL table or view name (for example `` `User` ``).
When a column is also called `name`, read the table name with `sqlName()` if you
are working with a raw `DBRepresentable` reference. Query DSL code uses
`sqlName()` internally, so `userAdapter.createIndexOn(…, User.name)` and similar
calls behave as expected.
