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

Generated: `User` (properties) and a `ModelAdapter<User>` you expose as an abstract `val` on the database.

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
