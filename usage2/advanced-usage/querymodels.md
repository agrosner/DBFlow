# Query models

`@Query` types are not tables. They only map a `SELECT` onto a class.

```kotlin
@Query
data class AuthorNameQuery(
    val blogName: String,
    val authorId: Int,
    val blogId: Int,
)
```

```kotlin
@Database(version = 1, tables = [Author::class, Blog::class], queries = [AuthorNameQuery::class])
abstract class AppDatabase : DBFlowDatabase<AppDatabase>() {
    abstract val blogAdapter: ModelAdapter<Blog>
    abstract val authorAdapter: ModelAdapter<Author>
    abstract val authorNameQueryAdapter: QueryAdapter<AuthorNameQuery>
}
```

```kotlin
db.readableTransaction {
    (blogAdapter.select(
        Blog_Table.name.withTable() `as` "blogName",
        Blog_Table.author_id.withTable() `as` "authorId",
        Blog_Table.id.withTable() `as` "blogId",
    ) innerJoin authorAdapter
        on (Blog_Table.author_id.withTable() eq Author_Table.id.withTable()))
        .list(authorNameQueryAdapter)
}
```

`@QueryModel` is a deprecated alias of `@Query`.
