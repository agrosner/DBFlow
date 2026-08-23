# Relationships

## Foreign key

```kotlin
@Table
data class Author(
    @PrimaryKey(autoincrement = true) val id: Int = 0,
    val firstName: String,
    val lastName: String,
)

@Table
data class Blog(
    @PrimaryKey(autoincrement = true) val id: Int = 0,
    val name: String,
    @ForeignKey(saveForeignKeyModel = true) val author: Author?,
)
```

DBFlow stores the author's primary key on `Blog` and can load `Author` when you read a `Blog`.

`saveForeignKeyModel = true` saves the parent first. Leave it `false` (default) if you persist the parent yourself.

Enable SQLite enforcement:

```kotlin
@Database(version = 1, foreignKeyConstraintsEnforced = true, tables = [Author::class, Blog::class])
```

Useful `@ForeignKey` flags: `onDelete` / `onUpdate` (`ForeignKeyAction`), `deferred`, `references` for a non-default column mapping.

## Many-to-many

Annotate one side. The plugin generates a join table (`Artist_Song` here).

```kotlin
@ManyToMany(referencedTable = Song::class)
@Table
data class Artist(
    @PrimaryKey(autoincrement = true) val id: Int = 0,
    val name: String,
)

@Table
data class Song(
    @PrimaryKey(autoincrement = true) val id: Int = 0,
    val name: String,
)
```

List the generated table if you declare tables explicitly, or look it up:

```kotlin
val row = Artist_Song(id = 0, artist = artist, song = song)
DatabaseObjectLookup.getModelAdapter(Artist_Song::class).save(row)
```

`generateAutoIncrement` (default `true`) adds a `Long` primary key. Set it `false` to use both sides' keys as a composite PK.

## One-to-many

`@OneToMany` is a method-level helper for loading children. Keep the child `@ForeignKey` on the many side; do not store a `List` column.
